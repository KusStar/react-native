/**
 * Copyright (c) 2015-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.react.uimanager;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.PopupMenu;
import com.facebook.react.R;
import com.facebook.infer.annotation.Assertions;
import com.facebook.react.animation.Animation;
import com.facebook.react.animation.AnimationListener;
import com.facebook.react.animation.AnimationRegistry;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.JSApplicationIllegalArgumentException;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.SoftAssertions;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.touch.JSResponderHandler;
import com.facebook.react.uimanager.common.SizeMonitoringFrameLayout;
import com.facebook.react.uimanager.layoutanimation.LayoutAnimationController;
import com.facebook.react.uimanager.layoutanimation.LayoutAnimationListener;
import com.facebook.systrace.Systrace;
import com.facebook.systrace.SystraceMessage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * Delegate of {@link UIManagerModule} that owns the native view hierarchy and mapping between
 * native view names used in JS and corresponding instances of {@link ViewManager}. The
 * {@link UIManagerModule} communicates with this class by it's public interface methods:
 *  - {@link #updateProperties}
 *  - {@link #updateLayout}
 *  - {@link #createView}
 *  - {@link #manageChildren}
 * executing all the scheduled UI operations at the end of JS batch.
 *
 * NB: All native view management methods listed above must be called from the UI thread.
 *
 * The {@link ReactContext} instance that is passed to views that this manager creates differs
 * from the one that we pass as a constructor. Instead we wrap the provided instance of
 * {@link ReactContext} in an instance of {@link ThemedReactContext} that additionally provide
 * a correct theme based on the root view for a view tree that we attach newly created view to.
 * Therefore this view manager will create a copy of {@link ThemedReactContext} that wraps
 * the instance of {@link ReactContext} for each root view added to the manager (see
 * {@link #addRootView}).
 *
 * TODO(5483031): Only dispatch updates when shadow views have changed
 */
@NotThreadSafe
public class NativeViewHierarchyManager {

  private static final String TAG = NativeViewHierarchyManager.class.getSimpleName();

  private final AnimationRegistry mAnimationRegistry;
  private final SparseArray<View> mTagsToViews;
  private final SparseArray<ViewManager> mTagsToViewManagers;
  private final SparseBooleanArray mRootTags;
  private final ViewManagerRegistry mViewManagers;
  private final JSResponderHandler mJSResponderHandler = new JSResponderHandler();
  private final RootViewManager mRootViewManager;
  private final LayoutAnimationController mLayoutAnimator = new LayoutAnimationController();

  private boolean mLayoutAnimationEnabled;
  private PopupMenu mPopupMenu;

  public NativeViewHierarchyManager(ViewManagerRegistry viewManagers) {
    this(viewManagers, new RootViewManager());
  }

  public NativeViewHierarchyManager(ViewManagerRegistry viewManagers, RootViewManager manager) {
    mAnimationRegistry = new AnimationRegistry();
    mViewManagers = viewManagers;
    mTagsToViews = new SparseArray<>();
    mTagsToViewManagers = new SparseArray<>();
    mRootTags = new SparseBooleanArray();
    mRootViewManager = manager;
  }

  public synchronized final View resolveView(int tag) {
    View view = mTagsToViews.get(tag);
    if (view == null) {
      throw new IllegalViewOperationException("Trying to resolve view with tag " + tag
          + " which doesn't exist");
    }
    return view;
  }

  public synchronized final ViewManager resolveViewManager(int tag) {
    ViewManager viewManager = mTagsToViewManagers.get(tag);
    if (viewManager == null) {
      throw new IllegalViewOperationException("ViewManager for tag " + tag + " could not be found");
    }
    return viewManager;
  }

  public AnimationRegistry getAnimationRegistry() {
    return mAnimationRegistry;
  }

  public void setLayoutAnimationEnabled(boolean enabled) {
    mLayoutAnimationEnabled = enabled;
  }

  public synchronized void updateInstanceHandle(int tag, long instanceHandle) {
    UiThreadUtil.assertOnUiThread();

    try {
      updateInstanceHandle(resolveView(tag), instanceHandle);
    } catch (IllegalViewOperationException e) {
      Log.e(TAG, "Unable to update properties for view tag " + tag, e);
    }
  }

  public synchronized void updateProperties(int tag, ReactStylesDiffMap props) {
    UiThreadUtil.assertOnUiThread();

    try {
      ViewManager viewManager = resolveViewManager(tag);
      View viewToUpdate = resolveView(tag);

      if (props != null) {
        viewManager.updateProperties(viewToUpdate, props);
      }
    } catch (IllegalViewOperationException e) {
      Log.e(TAG, "Unable to update properties for view tag " + tag, e);
    }
  }

  public synchronized void updateViewExtraData(int tag, Object extraData) {
    UiThreadUtil.assertOnUiThread();

    ViewManager viewManager = resolveViewManager(tag);
    View viewToUpdate = resolveView(tag);
    viewManager.updateExtraData(viewToUpdate, extraData);
  }

  public synchronized void updateLayout(
      int parentTag, int tag, int x, int y, int width, int height) {
    UiThreadUtil.assertOnUiThread();
    SystraceMessage.beginSection(
        Systrace.TRACE_TAG_REACT_VIEW,
        "NativeViewHierarchyManager_updateLayout")
        .arg("parentTag", parentTag)
        .arg("tag", tag)
        .flush();
    try {
      View viewToUpdate = resolveView(tag);

      // Even though we have exact dimensions, we still call measure because some platform views (e.g.
      // Switch) assume that method will always be called before onLayout and onDraw. They use it to
      // calculate and cache information used in the draw pass. For most views, onMeasure can be
      // stubbed out to only call setMeasuredDimensions. For ViewGroups, onLayout should be stubbed
      // out to not recursively call layout on its children: React Native already handles doing that.
      //
      // Also, note measure and layout need to be called *after* all View properties have been updated
      // because of caching and calculation that may occur in onMeasure and onLayout. Layout
      // operations should also follow the native view hierarchy and go top to bottom for consistency
      // with standard layout passes (some views may depend on this).

      viewToUpdate.measure(
          View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
          View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));

      // We update the layout of the ReactRootView when there is a change in the layout of its child.
      // This is required to re-measure the size of the native View container (usually a
      // FrameLayout) that is configured with layout_height = WRAP_CONTENT or layout_width =
      // WRAP_CONTENT
      //
      // This code is going to be executed ONLY when there is a change in the size of the Root
      // View defined in the js side. Changes in the layout of inner views will not trigger an update
      // on the layour of the Root View.
      ViewParent parent = viewToUpdate.getParent();
      if (parent instanceof RootView) {
        parent.requestLayout();
      }

      // Check if the parent of the view has to layout the view, or the child has to lay itself out.
      if (!mRootTags.get(parentTag)) {
        ViewManager parentViewManager = mTagsToViewManagers.get(parentTag);
        ViewGroupManager parentViewGroupManager;
        if (parentViewManager instanceof ViewGroupManager) {
          parentViewGroupManager = (ViewGroupManager) parentViewManager;
        } else {
          throw new IllegalViewOperationException(
              "Trying to use view with tag " + parentTag +
                  " as a parent, but its Manager doesn't extends ViewGroupManager");
        }
        if (parentViewGroupManager != null
            && !parentViewGroupManager.needsCustomLayoutForChildren()) {
          updateLayout(viewToUpdate, x, y, width, height);
        }
      } else {
        updateLayout(viewToUpdate, x, y, width, height);
      }
    } finally {
      Systrace.endSection(Systrace.TRACE_TAG_REACT_VIEW);
    }
  }

  @TargetApi(Build.VERSION_CODES.DONUT)
  private void updateInstanceHandle(View viewToUpdate, long instanceHandle) {
    UiThreadUtil.assertOnUiThread();
    viewToUpdate.setTag(R.id.view_tag_instance_handle, instanceHandle);
  }

  @Nullable
  @TargetApi(Build.VERSION_CODES.DONUT)
  public long getInstanceHandle(int reactTag) {
    View view = mTagsToViews.get(reactTag);
    if (view == null) {
      throw new IllegalViewOperationException("Unable to find view for tag: " + reactTag);
    }
    Long instanceHandle = (Long) view.getTag(R.id.view_tag_instance_handle);
    if (instanceHandle == null) {
      throw new IllegalViewOperationException("Unable to find instanceHandle for tag: " + reactTag);
    }
    return instanceHandle;
  }

  private void updateLayout(View viewToUpdate, int x, int y, int width, int height) {
    if (mLayoutAnimationEnabled &&
        mLayoutAnimator.shouldAnimateLayout(viewToUpdate)) {
      mLayoutAnimator.applyLayoutUpdate(viewToUpdate, x, y, width, height);
    } else {
      viewToUpdate.layout(x, y, x + width, y + height);
    }
  }

  public synchronized void createView(
      ThemedReactContext themedContext,
      int tag,
      String className,
      @Nullable ReactStylesDiffMap initialProps) {
    UiThreadUtil.assertOnUiThread();
    SystraceMessage.beginSection(
        Systrace.TRACE_TAG_REACT_VIEW,
        "NativeViewHierarchyManager_createView")
        .arg("tag", tag)
        .arg("className", className)
        .flush();
    try {
      ViewManager viewManager = mViewManagers.get(className);

      View view = viewManager.createView(themedContext, mJSResponderHandler);
      mTagsToViews.put(tag, view);
      mTagsToViewManagers.put(tag, viewManager);

      // Use android View id field to store React tag. This is possible since we don't inflate
      // React views from layout xmls. Thus it is easier to just reuse that field instead of
      // creating another (potentially much more expensive) mapping from view to React tag
      view.setId(tag);
      if (initialProps != null) {
        viewManager.updateProperties(view, initialProps);
      }
    } finally {
      Systrace.endSection(Systrace.TRACE_TAG_REACT_VIEW);
    }
  }

  private static String constructManageChildrenErrorMessage(
      ViewGroup viewToManage,
      ViewGroupManager viewManager,
      @Nullable int[] indicesToRemove,
      @Nullable ViewAtIndex[] viewsToAdd,
      @Nullable int[] tagsToDelete) {
    StringBuilder stringBuilder = new StringBuilder();

    if (null != viewToManage) {
      stringBuilder.append(
          "View tag:"
              + viewToManage.getId()
              + " View Type:"
              + viewToManage.getClass().toString()
              + "\n");
      stringBuilder.append("  children(" + viewManager.getChildCount(viewToManage) + "): [\n");
      for (int index = 0; viewManager.getChildAt(viewToManage, index) != null; index += 16) {
        for (int innerOffset = 0;
            viewManager.getChildAt(viewToManage, index + innerOffset) != null && innerOffset < 16;
            innerOffset++) {
          stringBuilder.append(
              viewManager.getChildAt(viewToManage, index + innerOffset).getId() + ",");
        }
        stringBuilder.append("\n");
      }
      stringBuilder.append(" ],\n");
    }

    if (indicesToRemove != null) {
      stringBuilder.append("  indicesToRemove(" + indicesToRemove.length + "): [\n");
      for (int index = 0; index < indicesToRemove.length; index += 16) {
        for (
            int innerOffset = 0;
            ((index + innerOffset) < indicesToRemove.length) && innerOffset < 16;
            innerOffset++) {
          stringBuilder.append(indicesToRemove[index + innerOffset] + ",");
        }
        stringBuilder.append("\n");
      }
      stringBuilder.append(" ],\n");
    }
    if (viewsToAdd != null) {
      stringBuilder.append("  viewsToAdd(" + viewsToAdd.length + "): [\n");
      for (int index = 0; index < viewsToAdd.length; index += 16) {
        for (
            int innerOffset = 0;
            ((index + innerOffset) < viewsToAdd.length) && innerOffset < 16;
            innerOffset++) {
          stringBuilder.append(
              "[" + viewsToAdd[index + innerOffset].mIndex + "," +
                  viewsToAdd[index + innerOffset].mTag + "],");
        }
        stringBuilder.append("\n");
      }
      stringBuilder.append(" ],\n");
    }
    if (tagsToDelete != null) {
      stringBuilder.append("  tagsToDelete(" + tagsToDelete.length + "): [\n");
      for (int index = 0; index < tagsToDelete.length; index += 16) {
        for (
            int innerOffset = 0;
            ((index + innerOffset) < tagsToDelete.length) && innerOffset < 16;
            innerOffset++) {
          stringBuilder.append(tagsToDelete[index + innerOffset] + ",");
        }
        stringBuilder.append("\n");
      }
      stringBuilder.append(" ]\n");
    }

    return stringBuilder.toString();
  }

  /**
   * @param tag react tag of the node we want to manage
   * @param indicesToRemove ordered (asc) list of indicies at which view should be removed
   * @param viewsToAdd ordered (asc based on mIndex property) list of tag-index pairs that represent
   * a view which should be added at the specified index
   * @param tagsToDelete list of tags corresponding to views that should be removed
   * @param indicesToDelete parallel list to tagsToDelete, list of indices of those tags
   */
  public synchronized void manageChildren(
      int tag,
      @Nullable int[] indicesToRemove,
      @Nullable ViewAtIndex[] viewsToAdd,
      @Nullable int[] tagsToDelete) {
    UiThreadUtil.assertOnUiThread();

    final Set<Integer> pendingDeletionTags = new HashSet<>();

    final ViewGroup viewToManage = (ViewGroup) mTagsToViews.get(tag);
    final ViewGroupManager viewManager = (ViewGroupManager) resolveViewManager(tag);
    if (viewToManage == null) {
      throw new IllegalViewOperationException("Trying to manageChildren view with tag " + tag +
        " which doesn't exist\n detail: " +
            constructManageChildrenErrorMessage(
                viewToManage,
                viewManager,
                indicesToRemove,
                viewsToAdd,
                tagsToDelete));
    }

    int lastIndexToRemove = viewManager.getChildCount(viewToManage);
    if (indicesToRemove != null) {
      for (int i = indicesToRemove.length - 1; i >= 0; i--) {
        int indexToRemove = indicesToRemove[i];
        if (indexToRemove < 0) {
          throw new IllegalViewOperationException(
              "Trying to remove a negative view index:"
                  + indexToRemove + " view tag: " + tag + "\n detail: " +
                  constructManageChildrenErrorMessage(
                      viewToManage,
                      viewManager,
                      indicesToRemove,
                      viewsToAdd,
                      tagsToDelete));
        }
        if (viewManager.getChildAt(viewToManage, indexToRemove) == null) {
          if (mRootTags.get(tag) && viewManager.getChildCount(viewToManage) == 0) {
            // This root node has already been removed (likely due to a threading issue caused by
            // async js execution). Ignore this root removal.
            return;
          }
          throw new IllegalViewOperationException(
              "Trying to remove a view index above child " +
                  "count " + indexToRemove + " view tag: " + tag + "\n detail: " +
                  constructManageChildrenErrorMessage(
                      viewToManage,
                      viewManager,
                      indicesToRemove,
                      viewsToAdd,
                      tagsToDelete));
        }
        if (indexToRemove >= lastIndexToRemove) {
          throw new IllegalViewOperationException(
              "Trying to remove an out of order view index:"
                  + indexToRemove + " view tag: " + tag + "\n detail: " +
                  constructManageChildrenErrorMessage(
                      viewToManage,
                      viewManager,
                      indicesToRemove,
                      viewsToAdd,
                      tagsToDelete));
        }

        View viewToRemove = viewManager.getChildAt(viewToManage, indexToRemove);

        if (mLayoutAnimationEnabled &&
            mLayoutAnimator.shouldAnimateLayout(viewToRemove) &&
            arrayContains(tagsToDelete, viewToRemove.getId())) {
          // The view will be removed and dropped by the 'delete' layout animation
          // instead, so do nothing
        } else {
          viewManager.removeViewAt(viewToManage, indexToRemove);
        }

        lastIndexToRemove = indexToRemove;
      }
    }

    if (tagsToDelete != null) {
      for (int i = 0; i < tagsToDelete.length; i++) {
        int tagToDelete = tagsToDelete[i];
        final View viewToDestroy = mTagsToViews.get(tagToDelete);
        if (viewToDestroy == null) {
          throw new IllegalViewOperationException(
            "Trying to destroy unknown view tag: "
              + tagToDelete + "\n detail: " +
              constructManageChildrenErrorMessage(
                viewToManage,
                viewManager,
                indicesToRemove,
                viewsToAdd,
                tagsToDelete));
        }

        if (mLayoutAnimationEnabled &&
          mLayoutAnimator.shouldAnimateLayout(viewToDestroy)) {
          pendingDeletionTags.add(tagToDelete);
          mLayoutAnimator.deleteView(
            viewToDestroy,
            new LayoutAnimationListener() {
              @Override
              public void onAnimationEnd() {
                viewManager.removeView(viewToManage, viewToDestroy);
                dropView(viewToDestroy);
                pendingDeletionTags.remove(viewToDestroy.getId());
              }
            });
        } else {
          dropView(viewToDestroy);
        }
      }
    }

    if (viewsToAdd != null) {
      for (int i = 0; i < viewsToAdd.length; i++) {
        ViewAtIndex viewAtIndex = viewsToAdd[i];
        View viewToAdd = mTagsToViews.get(viewAtIndex.mTag);
        if (viewToAdd == null) {
          throw new IllegalViewOperationException(
              "Trying to add unknown view tag: "
                  + viewAtIndex.mTag + "\n detail: " +
                  constructManageChildrenErrorMessage(
                      viewToManage,
                      viewManager,
                      indicesToRemove,
                      viewsToAdd,
                      tagsToDelete));
        }
        // viewManager.addView(viewToManage, viewToAdd, viewAtIndex.mIndex);
        int normalizedIndex = viewAtIndex.mIndex;
        if (!pendingDeletionTags.isEmpty()) {
          normalizedIndex = 0;
          int counter = 0;
          while (normalizedIndex < viewToManage.getChildCount()) {
            if (counter == viewAtIndex.mIndex) {
              break;
            }
            View v = viewToManage.getChildAt(normalizedIndex);
            if (!pendingDeletionTags.contains(v.getId())) {
              counter++;
            }
            normalizedIndex++;
          }
        }

        viewManager.addView(viewToManage, viewToAdd, normalizedIndex);
      }
    }
  }

  private boolean arrayContains(@Nullable int[] array, int ele) {
    if (array == null) {
      return false;
    }
    for (int curEle : array) {
      if (curEle == ele) {
        return true;
      }
    }
    return false;
  }

  /**
   * Simplified version of constructManageChildrenErrorMessage that only deals with adding children
   * views
   */
  private static String constructSetChildrenErrorMessage(
    ViewGroup viewToManage,
    ViewGroupManager viewManager,
    ReadableArray childrenTags) {
    ViewAtIndex[] viewsToAdd = new ViewAtIndex[childrenTags.size()];
    for (int i = 0; i < childrenTags.size(); i++) {
      viewsToAdd[i] = new ViewAtIndex(childrenTags.getInt(i), i);
    }
    return constructManageChildrenErrorMessage(
      viewToManage,
      viewManager,
      null,
      viewsToAdd,
      null
    );
  }

  /**
   * Simplified version of manageChildren that only deals with adding children views
   */
  public synchronized void setChildren(
    int tag,
    ReadableArray childrenTags) {
    UiThreadUtil.assertOnUiThread();
    ViewGroup viewToManage = (ViewGroup) mTagsToViews.get(tag);
    ViewGroupManager viewManager = (ViewGroupManager) resolveViewManager(tag);

    for (int i = 0; i < childrenTags.size(); i++) {
      View viewToAdd = mTagsToViews.get(childrenTags.getInt(i));
      if (viewToAdd == null) {
        throw new IllegalViewOperationException(
          "Trying to add unknown view tag: "
            + childrenTags.getInt(i) + "\n detail: " +
            constructSetChildrenErrorMessage(
              viewToManage,
              viewManager,
              childrenTags));
      }
      viewManager.addView(viewToManage, viewToAdd, i);
    }
  }

  /**
   * See {@link UIManagerModule#addRootView}.
   */
  public synchronized void addRootView(
      int tag,
      SizeMonitoringFrameLayout view,
      ThemedReactContext themedContext) {
    addRootViewGroup(tag, view, themedContext);
  }

  protected synchronized final void addRootViewGroup(
      int tag,
      ViewGroup view,
      ThemedReactContext themedContext) {
    if (view.getId() != View.NO_ID) {
      Log.e(
        TAG,
        "Trying to add a root view with an explicit id (" + view.getId() + ") already " +
        "set. React Native uses the id field to track react tags and will overwrite this field. " +
        "If that is fine, explicitly overwrite the id field to View.NO_ID before calling " +
        "addRootView.");
    }

    mTagsToViews.put(tag, view);
    mTagsToViewManagers.put(tag, mRootViewManager);
    mRootTags.put(tag, true);
    view.setId(tag);
  }

  /**
   * Releases all references to given native View.
   */
  protected synchronized void dropView(View view) {
    UiThreadUtil.assertOnUiThread();
    if (view == null) {
      // Ignore this drop operation when view is null.
      return;
    }
    if (mTagsToViewManagers.get(view.getId()) == null) {
      // This view has already been dropped (likely due to a threading issue caused by async js
      // execution). Ignore this drop operation.
      return;
    }
    if (!mRootTags.get(view.getId())) {
      // For non-root views we notify viewmanager with {@link ViewManager#onDropInstance}
      resolveViewManager(view.getId()).onDropViewInstance(view);
    }
    ViewManager viewManager = mTagsToViewManagers.get(view.getId());
    if (view instanceof ViewGroup && viewManager instanceof ViewGroupManager) {
      ViewGroup viewGroup = (ViewGroup) view;
      ViewGroupManager viewGroupManager = (ViewGroupManager) viewManager;
      for (int i = viewGroupManager.getChildCount(viewGroup) - 1; i >= 0; i--) {
        View child = viewGroupManager.getChildAt(viewGroup, i);
        if (child != null && mTagsToViews.get(child.getId()) != null) {
          dropView(child);
        }
      }
      viewGroupManager.removeAllViews(viewGroup);
    }
    mTagsToViews.remove(view.getId());
    mTagsToViewManagers.remove(view.getId());
  }

  public synchronized void removeRootView(int rootViewTag) {
    UiThreadUtil.assertOnUiThread();
    if (!mRootTags.get(rootViewTag)) {
        SoftAssertions.assertUnreachable(
            "View with tag " + rootViewTag + " is not registered as a root view");
    }
    View rootView = mTagsToViews.get(rootViewTag);
    dropView(rootView);
    mRootTags.delete(rootViewTag);
  }

  /**
   * Returns true on success, false on failure. If successful, after calling, output buffer will be
   * {x, y, width, height}.
   */
  public synchronized void measure(int tag, int[] outputBuffer) {
    UiThreadUtil.assertOnUiThread();
    View v = mTagsToViews.get(tag);
    if (v == null) {
      throw new NoSuchNativeViewException("No native view for " + tag + " currently exists");
    }

    View rootView = (View) RootViewUtil.getRootView(v);
    // It is possible that the RootView can't be found because this view is no longer on the screen
    // and has been removed by clipping
    if (rootView == null) {
      throw new NoSuchNativeViewException("Native view " + tag + " is no longer on screen");
    }
    rootView.getLocationInWindow(outputBuffer);
    int rootX = outputBuffer[0];
    int rootY = outputBuffer[1];

    v.getLocationInWindow(outputBuffer);

    outputBuffer[0] = outputBuffer[0] - rootX;
    outputBuffer[1] = outputBuffer[1] - rootY;
    outputBuffer[2] = v.getWidth();
    outputBuffer[3] = v.getHeight();
  }

  /**
   * Returns the coordinates of a view relative to the window (not just the RootView
   * which is what measure will return)
   *
   * @param tag - the tag for the view
   * @param outputBuffer - output buffer that contains [x,y,width,height] of the view in coordinates
   *  relative to the device window
   */
  public synchronized void measureInWindow(int tag, int[] outputBuffer) {
    UiThreadUtil.assertOnUiThread();
    View v = mTagsToViews.get(tag);
    if (v == null) {
      throw new NoSuchNativeViewException("No native view for " + tag + " currently exists");
    }

    v.getLocationOnScreen(outputBuffer);

    // We need to remove the status bar from the height.  getLocationOnScreen will include the
    // status bar.
    Resources resources = v.getContext().getResources();
    int statusBarId = resources.getIdentifier("status_bar_height", "dimen", "android");
    if (statusBarId > 0) {
      int height = (int) resources.getDimension(statusBarId);
      outputBuffer[1] -= height;
    }

    // outputBuffer[0,1] already contain what we want
    outputBuffer[2] = v.getWidth();
    outputBuffer[3] = v.getHeight();
  }

  public synchronized int findTargetTagForTouch(int reactTag, float touchX, float touchY) {
    UiThreadUtil.assertOnUiThread();
    View view = mTagsToViews.get(reactTag);
    if (view == null) {
      throw new JSApplicationIllegalArgumentException("Could not find view with tag " + reactTag);
    }
    return TouchTargetHelper.findTargetTagForTouch(touchX, touchY, (ViewGroup) view);
  }

  public synchronized void setJSResponder(
    int reactTag,
    int initialReactTag,
    boolean blockNativeResponder) {
    if (!blockNativeResponder) {
      mJSResponderHandler.setJSResponder(initialReactTag, null);
      return;
    }

    View view = mTagsToViews.get(reactTag);
    if (initialReactTag != reactTag && view instanceof ViewParent) {
      // In this case, initialReactTag corresponds to a virtual/layout-only View, and we already
      // have a parent of that View in reactTag, so we can use it.
      mJSResponderHandler.setJSResponder(initialReactTag, (ViewParent) view);
      return;
    }

    if (mRootTags.get(reactTag)) {
      SoftAssertions.assertUnreachable(
          "Cannot block native responder on " + reactTag + " that is a root view");
    }
    mJSResponderHandler
        .setJSResponder(initialReactTag, view.getParent());
  }

  public void clearJSResponder() {
    mJSResponderHandler.clearJSResponder();
  }

  void configureLayoutAnimation(final ReadableMap config) {
    mLayoutAnimator.initializeFromConfig(config);
  }

  void clearLayoutAnimation() {
    mLayoutAnimator.reset();
  }

  /* package */ synchronized void startAnimationForNativeView(
      int reactTag,
      Animation animation,
      @Nullable final Callback animationCallback) {
    UiThreadUtil.assertOnUiThread();
    View view = mTagsToViews.get(reactTag);
    final int animationId = animation.getAnimationID();
    if (view != null) {
      animation.setAnimationListener(new AnimationListener() {
        @Override
        public void onFinished() {
          Animation removedAnimation = mAnimationRegistry.removeAnimation(animationId);

          // There's a chance that there was already a removeAnimation call enqueued on the main
          // thread when this callback got enqueued on the main thread, but the Animation class
          // should handle only calling one of onFinished and onCancel exactly once.
          Assertions.assertNotNull(removedAnimation, "Animation was already removed somehow!");
          if (animationCallback != null) {
            animationCallback.invoke(true);
          }
        }

        @Override
        public void onCancel() {
          Animation removedAnimation = mAnimationRegistry.removeAnimation(animationId);

          Assertions.assertNotNull(removedAnimation, "Animation was already removed somehow!");
          if (animationCallback != null) {
            animationCallback.invoke(false);
          }
        }
      });
      animation.start(view);
    } else {
      // TODO(5712813): cleanup callback in JS callbacks table in case of an error
      throw new IllegalViewOperationException("View with tag " + reactTag + " not found");
    }
  }

  @Deprecated
  public synchronized void dispatchCommand(
    int reactTag,
    int commandId,
    @Nullable ReadableArray args) {
    UiThreadUtil.assertOnUiThread();
    View view = mTagsToViews.get(reactTag);
    if (view == null) {
      throw new IllegalViewOperationException("Trying to send command to a non-existing view " +
          "with tag " + reactTag);
    }

    ViewManager viewManager = resolveViewManager(reactTag);
    viewManager.receiveCommand(view, commandId, args);
  }

  public synchronized void dispatchCommand(
      int reactTag, String commandId, @Nullable ReadableArray args) {
    UiThreadUtil.assertOnUiThread();
    View view = mTagsToViews.get(reactTag);
    if (view == null) {
      throw new IllegalViewOperationException(
          "Trying to send command to a non-existing view with tag ["
              + reactTag
              + "] and command "
              + commandId);
    }
    ViewManager viewManager = resolveViewManager(reactTag);
    
    viewManager.receiveCommand(view, commandId, args);
  }

  /**
   * Show a {@link PopupMenu}.
   *
   * @param reactTag the tag of the anchor view (the PopupMenu is displayed next to this view); this
   *        needs to be the tag of a native view (shadow views can not be anchors)
   * @param items the menu items as an array of strings
   * @param success will be called with the position of the selected item as the first argument, or
   *        no arguments if the menu is dismissed
   */
  public synchronized void showPopupMenu(int reactTag, ReadableArray items, Callback success,
                                         Callback error) {
    UiThreadUtil.assertOnUiThread();
    View anchor = mTagsToViews.get(reactTag);
    if (anchor == null) {
      error.invoke("Can't display popup. Could not find view with tag " + reactTag);
      return;
    }
    mPopupMenu = new PopupMenu(getReactContextForView(reactTag), anchor);

    Menu menu = mPopupMenu.getMenu();
    for (int i = 0; i < items.size(); i++) {
      menu.add(Menu.NONE, Menu.NONE, i, items.getString(i));
    }

    PopupMenuCallbackHandler handler = new PopupMenuCallbackHandler(success);
    mPopupMenu.setOnMenuItemClickListener(handler);
    mPopupMenu.setOnDismissListener(handler);

    mPopupMenu.show();
  }

  /**
   * Dismiss the last opened PopupMenu {@link PopupMenu}.
   */
  public void dismissPopupMenu() {
    if (mPopupMenu != null) {
      mPopupMenu.dismiss();
    }
  }

  private static class PopupMenuCallbackHandler implements PopupMenu.OnMenuItemClickListener,
      PopupMenu.OnDismissListener {

    final Callback mSuccess;
    boolean mConsumed = false;

    private PopupMenuCallbackHandler(Callback success) {
      mSuccess = success;
    }

    @Override
    public void onDismiss(PopupMenu menu) {
      if (!mConsumed) {
        mSuccess.invoke(UIManagerModuleConstants.ACTION_DISMISSED);
        mConsumed = true;
      }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
      if (!mConsumed) {
        mSuccess.invoke(UIManagerModuleConstants.ACTION_ITEM_SELECTED, item.getOrder());
        mConsumed = true;
        return true;
      }
      return false;
    }
  }

  /**
   * @return Themed React context for view with a given {@param reactTag} -  it gets the
   * context directly from the view using {@link View#getContext}.
   */
  private ThemedReactContext getReactContextForView(int reactTag) {
    View view = mTagsToViews.get(reactTag);
    if (view == null) {
      throw new JSApplicationIllegalArgumentException("Could not find view with tag " + reactTag);
    }
    return (ThemedReactContext) view.getContext();
  }

  public void sendAccessibilityEvent(int tag, int eventType) {
    View view = mTagsToViews.get(tag);
    if (view == null) {
      throw new JSApplicationIllegalArgumentException("Could not find view with tag " + tag);
    }
    AccessibilityHelper.sendAccessibilityEvent(view, eventType);
  }
}
