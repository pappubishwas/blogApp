package com.example.blogapp.Fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.blogapp.databinding.FragmentBlogsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.blogapp.Adapter;
import com.example.blogapp.Model;
import java.util.ArrayList;

public class Blogs extends Fragment {

    private FragmentBlogsBinding binding;
    private ArrayList<Model> list;
    private Adapter adapter;

    public Blogs() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBlogsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setupRecyclerView();
        setupSearchView();
        super.onViewCreated(view, savedInstanceState);
    }

    private void setupSearchView() {
        binding.searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterBlogs(newText);
                return false;
            }
        });
    }

    private void filterBlogs(String newText) {
        ArrayList<Model> filteredList = new ArrayList<>();
        for (Model item : list) {
            if (item.getTittle().toLowerCase().contains(newText.toLowerCase())) {
                filteredList.add(item);
            }
        }
        adapter.filter_list(filteredList);
    }

    private void setupRecyclerView() {
        list = new ArrayList<>();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("Blogs")
                .whereEqualTo("userId", currentUserId)
                .orderBy("timestamp")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            return;
                        }
                        list.clear();
                        for (DocumentSnapshot snapshot : value.getDocuments()) {
                            Model model = snapshot.toObject(Model.class);
                            if (model != null) {
                                model.setId(snapshot.getId());
                                list.add(model);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });

        adapter = new Adapter(list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        binding.rvBlogs.setLayoutManager(layoutManager);
        binding.rvBlogs.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
