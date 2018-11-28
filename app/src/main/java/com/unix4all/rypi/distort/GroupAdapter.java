package com.unix4all.rypi.distort;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class GroupAdapter extends RecyclerView.Adapter<GroupViewHolder> {
    private ArrayList<DistortGroup> mGroupsData;
    private Context mContext;
    private DistortAuthParams mAuthParams;

    public GroupAdapter(Context context, ArrayList<DistortGroup> groups, DistortAuthParams authParams) {
        mGroupsData = groups;
        if(mGroupsData == null) {
            mGroupsData = new ArrayList<DistortGroup>();
        }
        mContext = context;
        mAuthParams = authParams;
    }

    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GroupViewHolder holder, int position) {
        DistortGroup g = mGroupsData.get(position);

        // Set group colour if active
        if(g.getIsActive().equals(new Boolean(true))) {
            holder.mGroupContainer.setBackgroundColor(mContext.getResources().getColor(R.color.activeGroupColour));
        } else {
            holder.mGroupContainer.setBackgroundColor(mContext.getResources().getColor(R.color.disabledGroupColour));
        }

        // Set Icon text and colour
        holder.mIcon.setText(g.getName().substring(0, 1));
        Random mRandom = new Random();
        final int colour = Color.argb(255, mRandom.nextInt(256), mRandom.nextInt(256), mRandom.nextInt(256));
        ((GradientDrawable) holder.mIcon.getBackground()).setColor(colour);

        // Set text fields
        holder.mName.setText(g.getName());
        final Integer heightCount = g.getHeight();
        holder.mDetails.setText(String.format(Locale.US, "%s: %d",
                mContext.getResources().getString(R.string.messages_total_height), heightCount));
        final Integer unreadCount = g.getHeight() - g.getLastReadIndex() - 1;
        holder.mUnread.setText(String.format(Locale.US, "%s: %d",
                mContext.getResources().getString(R.string.messages_unread), unreadCount));

        final GroupViewHolder gvp = holder;
        holder.mGroupContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(mContext, MessagingActivity.class);

                // Put authorization fields
                mIntent.putExtra(DistortAuthParams.EXTRA_HOMESERVER, mAuthParams.getHomeserverAddress());
                mIntent.putExtra(DistortAuthParams.EXTRA_HOMESERVER_PROTOCOL, mAuthParams.getHomeserverProtocol());
                mIntent.putExtra(DistortAuthParams.EXTRA_PEER_ID, mAuthParams.getPeerId());
                mIntent.putExtra(DistortAuthParams.EXTRA_ACCOUNT_NAME, mAuthParams.getAccountName());
                mIntent.putExtra(DistortAuthParams.EXTRA_CREDENTIAL, mAuthParams.getCredential());

                // Put group fields
                mIntent.putExtra("name", gvp.mName.getText().toString());
                mIntent.putExtra("height", heightCount.intValue());
                mIntent.putExtra("unread", unreadCount.intValue());
                mIntent.putExtra("icon", gvp.mIcon.getText().toString());
                mIntent.putExtra("colorIcon", colour);
                mContext.startActivity(mIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mGroupsData.size();
    }

    public void addOrUpdateGroup(DistortGroup group) {
        int position = mGroupsData.size();
        for(int i = 0; i < mGroupsData.size(); i++) {
            if(mGroupsData.get(i).getId().equals(group.getId())) {
                position = i;
                break;
            }
        }
        if(position == mGroupsData.size()) {
            mGroupsData.add(position, group);
            notifyItemInserted(position);
        } else {
            mGroupsData.get(position).setName(group.getName());
            mGroupsData.get(position).setSubgroupIndex(group.getSubgroupIndex());
            mGroupsData.get(position).setHeight(group.getHeight());
            mGroupsData.get(position).setLastReadIndex(group.getLastReadIndex());
            mGroupsData.get(position).setIsActive(group.getIsActive());

            notifyItemChanged(position);
        }
    }

    public void removeItem(int position) {
        mGroupsData.remove(position);
        notifyItemRemoved(position);
    }
}

class GroupViewHolder extends RecyclerView.ViewHolder {
    TextView mIcon;
    TextView mName;
    TextView mDetails;
    TextView mUnread;
    ConstraintLayout mGroupContainer;

    public GroupViewHolder(View itemView) {
        super(itemView);

        mIcon = itemView.findViewById(R.id.groupIcon);
        mName = itemView.findViewById(R.id.groupName);
        mDetails = itemView.findViewById(R.id.groupDetail);
        mUnread = itemView.findViewById(R.id.groupUnread);
        mGroupContainer = itemView.findViewById(R.id.groupContainer);
    }
}