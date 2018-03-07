package com.example.marwa.worldnews.fragments;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.marwa.worldnews.News;
import com.example.marwa.worldnews.NewsLoader;
import com.example.marwa.worldnews.R;
import com.example.marwa.worldnews.adapters.NewsAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class LifestyleFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<News>> {

    /**
     * URL for News data from the Guardian
     */
    private static final String NEWS_REQUEST_URL = "http://content.guardianapis.com/search?section=lifeandstyle&show-tags=contributor&show-fields=thumbnail%2CtrailText&page-size=20&order-by=newest&api-key=37b5b5aa-d7fb-4219-b589-3e1af665cfff";
    /**
     * Constant value for the NEWS loader ID.
     */
    private static final int NEWS_LOADER_ID = 1;
    /**
     * Adapter for the list of news stories.
     */
    NewsAdapter adapter;
    /**
     * TextView that is displayed when the list is empty.
     */
    private TextView emptyStateTextView;
    /**
     * a ProgressBar variable to show and hide the progress bar.
     */
    private ProgressBar loadingIndicator;

    NetworkInfo networkInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.news_list, container, false);

        // Find a reference to the {@link ListView} in the layout.
        ListView newsListView = (ListView) rootView.findViewById(R.id.newsList);

        // Find a reference to an empty TextView
        emptyStateTextView = (TextView) rootView.findViewById(R.id.empty_view);
        // Set the TextView on the ListView.
        newsListView.setEmptyView(emptyStateTextView);

        //Find the ProgressBar using findViewById.
        loadingIndicator = (ProgressBar) rootView.findViewById(R.id.loading_indicator);

        // Create a new adapter that takes an empty list of news stories as input.
        adapter = new NewsAdapter(getActivity(), new ArrayList<News>());

        // Set the adapter on the {@link ListView}.
        newsListView.setAdapter(adapter);

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected news story.
        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current News's story that was clicked on.
                News currentNewsStory = adapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor).
                Uri newsUri = null;
                if (currentNewsStory != null) {
                    newsUri = Uri.parse(currentNewsStory.getWebUrl());
                }

                // Create a new intent to view the News's story URI.
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);

                // Send the intent to launch a new activity.
                startActivity(websiteIntent);
            }
        });

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();
            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter.
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        } else {
            // Update empty state with no connection error message
            emptyStateTextView.setText(R.string.no_internet_connection);
        }

        return rootView;
    }

    @Override
    public Loader<List<News>> onCreateLoader(int id, Bundle args) {
        emptyStateTextView.setVisibility(View.INVISIBLE);
        // First, hide loading indicator.
        loadingIndicator.setVisibility(View.VISIBLE);
        // Create a new loader for the given URL.
        return new NewsLoader(getContext(), NEWS_REQUEST_URL);
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> data) {
        //Hide the indicator after the data is appeared
        loadingIndicator.setVisibility(View.GONE);

        // Check if connection is still available, otherwise show appropriate message
        if (networkInfo != null && networkInfo.isConnected()) {
            // If there is a valid list of news stories, then add them to the adapter's
            // data set. This will trigger the ListView to update.
            if (data != null && !data.isEmpty()) {
                adapter.addAll(data);
            }else {
                emptyStateTextView.setVisibility(View.VISIBLE);
                emptyStateTextView.setText(getString(R.string.no_news));
            }

        } else {
            emptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        // Loader reset, so we can clear out our existing data.
        adapter.clear();
    }
}
