package com.nuhkoca.udacitybakingapp.view.recipe.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nuhkoca.udacitybakingapp.R;
import com.nuhkoca.udacitybakingapp.callback.IRecipeItemClickListener;
import com.nuhkoca.udacitybakingapp.databinding.FragmentRecipeBinding;
import com.nuhkoca.udacitybakingapp.helper.Constants;
import com.nuhkoca.udacitybakingapp.model.RecipeResponse;
import com.nuhkoca.udacitybakingapp.presenter.recipe.fragment.RecipeFragmentPresenter;
import com.nuhkoca.udacitybakingapp.presenter.recipe.fragment.RecipeFragmentPresenterImpl;
import com.nuhkoca.udacitybakingapp.util.SnackbarPopper;
import com.nuhkoca.udacitybakingapp.view.recipe.adapter.RecipeAdapter;
import com.nuhkoca.udacitybakingapp.view.steps.activity.StepsActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class RecipeFragment extends Fragment implements RecipeFragmentView, IRecipeItemClickListener {

    private FragmentRecipeBinding mFragmentRecipeBinding;
    private RecipeFragmentPresenter mFragmentPresenter;

    private static List<RecipeResponse> mRecipeResponses;
    private static int mProgressRecipeVisibility;

    public static RecipeFragment getInstance() {

        return new RecipeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mFragmentRecipeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_recipe, container, false);

        return mFragmentRecipeBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mFragmentPresenter = new RecipeFragmentPresenterImpl(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mFragmentRecipeBinding.rvRecipes.setLayoutManager(linearLayoutManager);

        mFragmentRecipeBinding.rvRecipes.setNestedScrollingEnabled(false);
        mFragmentRecipeBinding.rvRecipes.setHasFixedSize(true);

        if (savedInstanceState != null) {
            RecipeAdapter recipeAdapter = new RecipeAdapter(mRecipeResponses, this);
            mFragmentRecipeBinding.rvRecipes.setAdapter(recipeAdapter);
            recipeAdapter.swapData();

            mFragmentRecipeBinding.pbRecipes.setVisibility(mProgressRecipeVisibility == 0 ? View.VISIBLE : View.GONE);

            return;
        }

        mFragmentPresenter.fetchRecipes();
    }

    @Override
    public void onRecipesFetchingSuccess(List<RecipeResponse> recipeResponses) {
        RecipeAdapter recipeAdapter = new RecipeAdapter(recipeResponses, this);
        mFragmentRecipeBinding.rvRecipes.setAdapter(recipeAdapter);
        recipeAdapter.swapData();

        mRecipeResponses = recipeResponses;
    }

    @Override
    public void onRecipesFetchingError(String message) {
        SnackbarPopper.pop(mFragmentRecipeBinding.flRecipes, message);
    }

    @Override
    public void onProgressVisibility(final boolean visible) {
        int duration = 0;

        if (getActivity() != null) {
            duration = getActivity().getResources().getInteger(android.R.integer.config_longAnimTime);
        }

        mFragmentRecipeBinding.pbRecipes.setVisibility(visible ? View.VISIBLE : View.GONE);
        mProgressRecipeVisibility = mFragmentRecipeBinding.pbRecipes.getVisibility();

        mFragmentRecipeBinding.pbRecipes.animate()
                .setDuration(duration)
                .alpha(visible ? 1 : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mFragmentRecipeBinding.pbRecipes.setVisibility(visible ? View.VISIBLE : View.GONE);
                    }
                });
    }

    @Override
    public void onRecipeItemClick(RecipeResponse recipeResponse) {
        Intent stepsIntent = new Intent(getActivity(), StepsActivity.class);
        stepsIntent.putExtra(Constants.RECIPE_MODEL_INTENT_EXTRA, recipeResponse);

        if (getActivity() != null)
            getActivity().startActivityForResult(stepsIntent, Constants.CHILD_ACTIVITY_REQUEST_CODE);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mFragmentPresenter = new RecipeFragmentPresenterImpl(this);
    }

    @Override
    public void onDetach() {
        mFragmentPresenter.destroyView();
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelableArrayList(Constants.RECIPE_MODEL_STATE, (ArrayList<? extends Parcelable>) mRecipeResponses);
        outState.putInt(Constants.PROGRESS_RECIPE_VISIBILITY_STATE, mProgressRecipeVisibility);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            mRecipeResponses = savedInstanceState.getParcelableArrayList(Constants.RECIPE_MODEL_STATE);
            mProgressRecipeVisibility = savedInstanceState.getInt(Constants.PROGRESS_RECIPE_VISIBILITY_STATE);
        }
    }
}