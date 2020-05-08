package com.navigation.reactnative;

import android.app.Activity;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.core.app.SharedElementCallback;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class FragmentNavigator extends SceneNavigator {

    @Override
    boolean canNavigate(Activity activity, NavigationStackView stack) {
        return !getFragmentManager(stack, activity).isStateSaved();
    }

    @Override
    void navigateBack(int currentCrumb, int crumb, Activity activity, NavigationStackView stack) {
        FragmentManager fragmentManager = getFragmentManager(stack, activity);
        SceneFragment fragment = (SceneFragment) fragmentManager.findFragmentByTag(oldKey);
        Pair[] sharedElements = fragment != null ? getOldSharedElements(currentCrumb, crumb, fragment, stack) : null;
        SceneFragment prevFragment = (SceneFragment) fragmentManager.findFragmentByTag(stack.keys.getString(crumb));
        if (fragment != null) {
            if (sharedElements != null && prevFragment != null && prevFragment.getScene() != null)
                prevFragment.getScene().transitioner = new SharedElementTransitioner(prevFragment, getSharedElementSet(stack.oldSharedElementNames));
        }

        for (int i = currentCrumb; i > crumb + 1; i--) {
            SceneFragment currentFragment = (SceneFragment) fragmentManager.findFragmentByTag(String.valueOf(i));
            if (currentFragment == null) {
                continue;
            }
            currentFragment.animationDisabled = true; // disable all intermediate animations
        }
        fragmentManager.popBackStack(String.valueOf(crumb), 0);
        if (prevFragment != null && prevFragment.getScene() != null) {
            prevFragment.getScene().appeared();
        }
    }

    @Override
    void navigate(int currentCrumb, int crumb, Activity activity, NavigationStackView stack) {
        final FragmentManager fragmentManager = getFragmentManager(stack, activity);
        int enter = getAnimationResourceId(activity, stack.enterAnim, android.R.attr.activityOpenEnterAnimation);
        int exit = getAnimationResourceId(activity, stack.exitAnim, android.R.attr.activityOpenExitAnimation);
        if (exit == 0 && stack.exitAnim != null)
            exit = getAnimationResourceId(activity, null, android.R.attr.activityOpenExitAnimation);
        for(int i = 0; i < crumb - currentCrumb; i++) {
            int nextCrumb = currentCrumb + i + 1;
            String key = stack.keys.getString(nextCrumb);
            final SceneView scene = stack.scenes.get(key);
            if (scene == null) {
                continue;
            }
            int popEnter = getAnimationResourceId(activity, scene.enterAnim, android.R.attr.activityCloseEnterAnimation);
            int popExit = getAnimationResourceId(activity, scene.exitAnim, android.R.attr.activityCloseExitAnimation);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setReorderingAllowed(true);
            Pair[] sharedElements = null;
            if (nextCrumb > 0) {
                String prevKey = stack.keys.getString(nextCrumb - 1);
                SceneFragment prevFramgent = (SceneFragment) fragmentManager.findFragmentByTag(prevKey);
                if (prevFramgent != null)
                    sharedElements = getSharedElements(currentCrumb, crumb, prevFramgent, stack);
            }
            if (sharedElements != null) {
                for(Pair sharedElement : sharedElements) {
                    fragmentTransaction.addSharedElement((View) sharedElement.first, (String) sharedElement.second);
                }
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            } else {
                if (stack.enterAnim != null && scene.exitAnim != null) {
                    // use the anim pass in
                    fragmentTransaction.setCustomAnimations(oldCrumb != -1 ? enter : 0, exit, popEnter, popExit);
                } else {
                    // use the custom stationary anim, not the system default anim, system default anim do not work properly in some huwei devices
                    fragmentTransaction.setCustomAnimations(oldCrumb != -1 ? R.anim.push_left_in : 0, exit, popEnter, R.anim.push_right_out);
                }
            }
            SceneFragment fragment = new SceneFragment(scene, getSharedElementSet(stack.sharedElementNames));
            fragmentTransaction.add(stack.getChildAt(0).getId(), fragment, key);
            fragmentTransaction.addToBackStack(String.valueOf(nextCrumb));
            fragmentTransaction.commitAllowingStateLoss();
            if (i == crumb - currentCrumb - 1 && oldCrumb != -1 && enter != 0) {
                Animation animation = AnimationUtils.loadAnimation(activity, enter);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scene.appeared();
                    }
                }, animation.getDuration());
            } else {
                scene.appeared();
            }
        }
        for (int i = 0; i <= currentCrumb; i++) {
            String key = stack.keys.getString(i);
            SceneView scene = stack.scenes.get(key);
            if (scene != null) {
                scene.disappeared();
            }
        }
    }

    @Override
    void refresh(int currentCrumb, int crumb, Activity activity, NavigationStackView stack) {
        int enter = getAnimationResourceId(activity, stack.enterAnim, android.R.attr.activityOpenEnterAnimation);
        int exit = getAnimationResourceId(activity, stack.exitAnim, android.R.attr.activityOpenExitAnimation);
        String key = stack.keys.getString(crumb);
        SceneView scene = stack.scenes.get(key);
        if (scene == null) {
            return;
        }
        int popEnter = getAnimationResourceId(activity, scene.enterAnim, android.R.attr.activityCloseEnterAnimation);
        int popExit = getAnimationResourceId(activity, scene.exitAnim, android.R.attr.activityCloseExitAnimation);
        FragmentManager fragmentManager = getFragmentManager(stack, activity);
        fragmentManager.popBackStack();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(enter, exit, popEnter, popExit);
        fragmentTransaction.add(stack.getChildAt(0).getId(), new SceneFragment(scene, null), key);
        fragmentTransaction.addToBackStack(String.valueOf(crumb));
        fragmentTransaction.commitAllowingStateLoss();
    }

    private FragmentManager getFragmentManager(NavigationStackView stack,  Activity activity) {
        ViewParent parent = stack;
        Fragment fragment = null;
        while (parent != null) {
            if (parent instanceof NavigationBoundary) {
                fragment = ((NavigationBoundary) parent).getFragment();
                break;
            }
            parent = parent.getParent();
        }
        return fragment == null? ((FragmentActivity) activity).getSupportFragmentManager():  fragment.getChildFragmentManager();
    }

    private Pair[] getOldSharedElements(int currentCrumb, int crumb, SharedElementContainer sharedElementContainer, final NavigationStackView stack) {
        final HashMap<String, View> oldSharedElementsMap = getSharedElementMap(sharedElementContainer.getScene());
        final Pair[] oldSharedElements = currentCrumb - crumb == 1 ? getSharedElements(oldSharedElementsMap, stack.oldSharedElementNames) : null;
        if (oldSharedElements != null && oldSharedElements.length != 0) {
            sharedElementContainer.setEnterCallback(new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> elements) {
                    for(int i = 0; i < stack.oldSharedElementNames.size(); i++) {
                        String name = stack.oldSharedElementNames.getString(i);
                        if (oldSharedElementsMap.containsKey(name)) {
                            View oldSharedElement = oldSharedElementsMap.get(name);
                            elements.put(names.get(i), oldSharedElement);
                        }
                    }
                }
            });
            return oldSharedElements;
        }
        return null;
    }

    private Pair[] getSharedElements(int currentCrumb, int crumb, SharedElementContainer sharedElementContainer, final NavigationStackView stack) {
        final HashMap<String, View> sharedElementsMap = getSharedElementMap(sharedElementContainer.getScene());
        final Pair[] sharedElements = crumb - currentCrumb == 1 ? getSharedElements(sharedElementsMap, stack.sharedElementNames) : null;
        if (sharedElements != null && sharedElements.length != 0) {
            sharedElementContainer.setExitCallback(new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> elements) {
                    for(int i = 0; i < names.size(); i++) {
                        String mappedName = names.get(i);
                        if (stack.oldSharedElementNames != null && stack.oldSharedElementNames.size() > i)
                            mappedName = stack.oldSharedElementNames.getString(i);
                        if (sharedElementsMap.containsKey(mappedName))
                            elements.put(names.get(i), sharedElementsMap.get(mappedName));
                    }
                }
            });
            return sharedElements;
        }
        return null;
    }
}
