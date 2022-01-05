package com.example.dadwalsocialmedia.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dadwalsocialmedia.CommentsActivity
import com.example.dadwalsocialmedia.R
import com.example.dadwalsocialmedia.models.Post
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.github.thunder413.datetimeutils.DateTimeStyle
import com.github.thunder413.datetimeutils.DateTimeUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FeedAdapter(options: FirestoreRecyclerOptions<Post>, val context: Context):
    FirestoreRecyclerAdapter<Post, FeedAdapter.FeedViewHolder>(options) {

        class FeedViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
            val postImage: ImageView = itemView.findViewById(R.id.post_image)
            val postText: TextView = itemView.findViewById(R.id.post_text)
            val authorText: TextView = itemView.findViewById(R.id.post_author)
            val timeText: TextView = itemView.findViewById(R.id.post_time)
            val likeIcon: ImageView = itemView.findViewById(R.id.like_icon)
            val likeCount: TextView = itemView.findViewById(R.id.like_count)
            val commentCount: TextView = itemView.findViewById(R.id.comment_count)
            val commentIcon: ImageView = itemView.findViewById(R.id.comment_icon)

        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post,parent,false)
        return FeedViewHolder(view)

    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int, model: Post) {

        val date = DateTimeUtils.formatDate(model.time)
        val dateFormatted = DateTimeUtils.formatWithStyle(date, DateTimeStyle.LONG)

        holder.postText.text = model.text
        holder.authorText.text = model.user.name
        holder.timeText.text = dateFormatted
        holder.likeCount.text = model.likesList.size.toString()

        Glide.with(context)
            .load(model.imageUrl)
            .centerCrop()
            .placeholder(R.drawable.placeholder_image)
            .into(holder.postImage)

        val firestore = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val postDocument = firestore.collection("Posts").document(snapshots.getSnapshot(holder.adapterPosition).id)

        postDocument.collection("Comments").get().addOnCompleteListener{
            if(it.isSuccessful){
                holder.commentCount.text = it.result?.size().toString()
            }
        }

        postDocument.get().addOnCompleteListener{ task ->
            if(task.isSuccessful){
                val post = task.result?.toObject(Post::class.java)
                post?.likesList?.let{ list ->
                    if(list.contains(userId)){
                        holder.likeIcon.setImageDrawable(ContextCompat.getDrawable(context,
                            R.drawable.like_icon_filled
                        )
                        )
                    } else{
                        holder.likeIcon.setImageDrawable(
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.like_icon_outline
                            )
                        )
                    }
                    holder.likeIcon.setOnClickListener{
                        if(post.likesList.contains(userId)){
                            post.likesList.remove(userId)
                            holder.likeIcon.setImageDrawable(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.like_icon_outline
                                )
                            )
                        }
                        else {
                            userId?.let{ userId ->
                                post.likesList.add(userId)
                            }
                            holder.likeIcon.setImageDrawable(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.like_icon_filled
                                )
                            )
                        }
                        postDocument.set(post)
                    }

                }


            } else {
                Toast.makeText(context, "Something went wrong. Please try again.", Toast.LENGTH_LONG).show()
            }

        }
        holder.commentIcon.setOnClickListener{
            val intent = Intent(context, CommentsActivity::class.java)
            intent.putExtra("postId", snapshots.getSnapshot(holder.adapterPosition).id)
            context.startActivity(intent)

        }
    }
}