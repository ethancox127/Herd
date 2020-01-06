package com.example.herd;

import android.view.View;

public interface OnItemClickListener {
    void onRowClick(View view, PostAdapter.PostViewHolder holder, int position);
}
