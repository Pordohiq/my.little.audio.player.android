package my.little.audio.player.android.FileView;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 Licence:
// https://github.com/Pordohiq/my.little.audio.player.android

import android.content.Context;

import android.util.AttributeSet;

import android.view.LayoutInflater;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import my.little.audio.player.android.Action.Action;
import my.little.audio.player.android.Global;
import my.little.audio.player.android.R;
import my.little.audio.player.android.Signals;
import my.little.audio.player.android.queues.Queue;
import my.little.audio.player.android.queues.Queues;

public class QueueBlock extends LinearLayout {
	// Nodes
	private LinearLayout block;
	private ImageView queueIcon;
	private TextView queueName;
	private ImageView moreIcon;
	// Data
	private Queue queue;
	
	public QueueBlock(Context context) {
		super(context);
		init(context);
	}
	
	public QueueBlock(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context){
		LayoutInflater.from(context).inflate(R.layout.queue_block_layout, this, true);
		block = findViewById(R.id.queue_block);
		
		queueIcon = findViewById(R.id.queue_icon);
		queueName = findViewById(R.id.queue_name);
		moreIcon = findViewById(R.id.more_icon);
		
		Signals.subscribeToEvent("onActionElementChanged", this::check_more_icon);
		Signals.subscribeToEvent("onLockStateChanged", this::on_lockState_changed);
		Signals.subscribeToEvent("onQueueSet", this::check_main_icon);
		on_lockState_changed();
	}
	
	private void check_main_icon() {
		Queue ac_que = Queues.get_active_queue();
		if (queue != null && (ac_que == queue)) {
			queueIcon.setImageResource(R.drawable.block_queue_active);
		} else {
			queueIcon.setImageResource(R.drawable.block_queue);
		}
	}
	
	private void check_more_icon() {
		if (queue == Action.get_queue() && queue != null) {
			moreIcon.setImageResource(R.drawable.block_more_active);
		} else {
			moreIcon.setImageResource(R.drawable.block_more);
		}
	}
	
	public void setUp(Queue new_queue) {
		queue = new_queue;
		queueName.setText(queue.get_name());
		
		// Onclick
		queueIcon.setOnClickListener(view -> mainClick());
		queueName.setOnClickListener(view -> mainClick());
		moreIcon.setOnClickListener(view -> secondClick());
		
		queueIcon.setOnLongClickListener(view -> longPress());
		queueName.setOnLongClickListener(view -> longPress());
		
		// Connection with ACTION
		Signals.subscribeToEvent("onActionQueueChanged", this::check_more_icon);
		check_more_icon();
		Signals.subscribeToEvent("onQueueSet", this::check_main_icon);
		check_main_icon();
	}
	
	private void mainClick(){
		if (Action.get_lockState() == Action.LockState.ALL || Action.get_lockState() == Action.LockState.QUEUE) return; // If locked, do nothing
		if (queue == null) return;
		Queues.set_active_queue(queue);
	}
	
	private void secondClick() {
		if (Action.get_lockState() == Action.LockState.ALL || Action.get_lockState() == Action.LockState.QUEUE) return; // If locked, do nothing
		Action.set_queue(queue);
	}
	
	private boolean longPress() {
		if (Action.get_lockState() == Action.LockState.ALL || Action.get_lockState() == Action.LockState.QUEUE) return false; // If locked, do nothing
		Queues.set_active_queue(queue);
		Global.setDisplayState(Global.DisplayState.QUEUE_CONTENT);
		return true;
	}
	
	private void on_lockState_changed(){
		if (Action.get_lockState() == Action.LockState.ALL || Action.get_lockState() == Action.LockState.QUEUE) {
			block.setAlpha(0.5f);
		}
		else {
			block.setAlpha(1f);
		}
	}
	
	@Override
	protected void onDetachedFromWindow() {
		if (queue == Action.get_queue() && queue != null){
			Action.unset_queue();
		}
		super.onDetachedFromWindow();
	}
}
