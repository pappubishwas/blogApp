package com.example.blogapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private List<Comment> commentList;

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        if (comment != null) {
            holder.username.setText(comment.getUsername() != null ? comment.getUsername() : "Unknown User");
            holder.commentText.setText(comment.getCommentText() != null ? comment.getCommentText() : "No comment");
            holder.timestamp.setText(comment.getTimestamp() != null ? comment.getTimestamp() : "N/A");
        }
    }

    @Override
    public int getItemCount() {
        return commentList != null ? commentList.size() : 0;
    }

    public void setComments(List<Comment> newComments) {
        this.commentList = newComments;
        notifyDataSetChanged();  // Refresh adapter
    }

    public void addComment(Comment comment) {
        commentList.add(comment);
        notifyItemInserted(commentList.size() - 1);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView username, commentText, timestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.textUsername);
            commentText = itemView.findViewById(R.id.textComment);
            timestamp = itemView.findViewById(R.id.textTimestamp);
        }
    }
}