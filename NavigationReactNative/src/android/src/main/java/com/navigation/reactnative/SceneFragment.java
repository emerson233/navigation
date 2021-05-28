package com.navigation.reactnative;

import android.os.Bundle;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import java.util.HashSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.SharedElementCallback;
import androidx.fragment.app.Fragment;

public class SceneFragment extends Fragment implements SharedElementContainer {
    public boolean animationDisabled;
    private SceneView scene;

    SceneFragment(SceneView scene, HashSet<String> sharedElements) {
        super();
        this.scene = scene;
        scene.fragmentMode = true;
        if (sharedElements != null )
            scene.transitioner = new SharedElementTransitioner(this, sharedElements);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (scene.getParent() != null) {
            ((ViewGroup) scene.getParent()).endViewTransition(scene);
        }
        if (scene.getParent() != null) {
            ((ViewGroup) scene.getParent()).removeView(scene);
        }
        if (scene.transitioner != null)
            postponeEnterTransition();
        return scene;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (scene != null) {
            scene.disappeared();
            scene.popped();
        }
    }

    @Override
    public SceneView getScene() {
        return scene;
    }

    @Override
    public boolean canAddTarget() {
        return false;
    }

    @Override
    public void setEnterTransition(Transition transition) {
        setSharedElementEnterTransition(transition);
    }

    @Override
    public void setExitCallback(SharedElementCallback sharedElementCallback) {
        setExitSharedElementCallback(sharedElementCallback);
    }

    @Override
    public void setEnterCallback(SharedElementCallback sharedElementCallback) {
        setEnterSharedElementCallback(sharedElementCallback);
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (animationDisabled) {
            Animation a = new Animation() {};
            a.setDuration(0);
            return a;
        }
        return super.onCreateAnimation(transit, enter, nextAnim);
    }
}
