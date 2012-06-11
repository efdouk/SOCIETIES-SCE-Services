package com.asocom.activities;

import java.util.Calendar;
import java.util.Date;

import com.asocom.components.ChatComponent;
import com.asocom.components.ChatMsg;
import com.asocom.components.Menu;
import com.asocom.components.TopTitre;
import com.asocom.model.Manager;
import com.asocom.model.SmsChat;
import com.asocom.tools.Json;
import com.asocom.tools.NonExistentCommunityException;
import com.asocom.tools.Server;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;

/**
 * 
 */
public class ComunityChatActivity extends Activity implements
		View.OnClickListener {

	private Menu menu;
	private LinearLayout layout;
	private ChatComponent chatComponent;
	private final int CONTEXT_MENU_ID = 7;

	//
	private static final String ACTIVITY_NAME = "ComunityChatActivity";

	/**
	 * 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_void);
		//
		LinearLayout topLayout = (LinearLayout) findViewById(R.id.main_void_topLayout);
		layout = (LinearLayout) findViewById(R.id.main_void_layout);
		//
		menu = new Menu(this);
		topLayout.addView(menu);
		TopTitre topTitre = new TopTitre(this);

		try {
			topTitre.setTextTitre(Manager.getCommunities()
					.get(Manager.getCurrentCommunityPos()).getCommunityName()
					+ "'s Chat");
			topLayout.addView(topTitre);

			chatComponent = new ChatComponent(this);
			topLayout.addView(chatComponent);
			chatComponent.getButtonSend().setOnClickListener(this);
			//
			loadAllUsers();
		} catch (NonExistentCommunityException e) {
			finish();
			Intent i = new Intent(this, HomeMenu.class);
			startActivity(i);
		}

		//
		Manager.setNameCurrentActivity(ACTIVITY_NAME);
		Manager.setCurrentActivity(this);

	}

	public void loadAllUsers() throws NonExistentCommunityException {
		chatComponent.resetChat();

		for (int i = Manager.getCommunities()
				.get(Manager.getCurrentCommunityPos()).getChat().getSmsList()
				.size() - 1; i >= 0; i--) {

			ChatMsg cm = new ChatMsg(this);
			cm.setDate(Manager.getCommunities()
					.get(Manager.getCurrentCommunityPos()).getChat()
					.getSmsList().get(i).getDateSms());
			cm.setTextChat(
					Manager.getCommunities()
							.get(Manager.getCurrentCommunityPos()).getChat()
							.getSmsList().get(i).getUser(),
					Manager.getCommunities()
							.get(Manager.getCurrentCommunityPos()).getChat()
							.getSmsList().get(i).getMessage());
			cm.setImageUser(Manager.getCommunities()
					.get(Manager.getCurrentCommunityPos()).getChat()
					.getSmsList().get(i).getImage());
			chatComponent.addChatMsg(cm);
		}
	}

	/**
	*
	*/
	public void onClick(View v) {

		if (v.getId() == R.id.chat_button
				&& !chatComponent.getEditText().toString().equals("")) {

			try {
				String dt;
				Date cal = Calendar.getInstance().getTime();
				dt = cal.toLocaleString();
				SmsChat smsChat = new SmsChat(Integer.parseInt(Manager
						.getCurrentPhoneUser().get(6)), dt, Manager
						.getCurrentPhoneUser().get(0), chatComponent.getText());
				Manager.getCommunities().get(Manager.getCurrentCommunityPos())
						.getChat().addSms(smsChat);
				Server.sendData(Json.sendCommunityChat(Manager
						.getCurrentCommunity().getId(), chatComponent.getText()));
				chatComponent.getEditText().setText("");
				loadAllUsers();
			} catch (NonExistentCommunityException e) {
				finish();
				Intent i = new Intent(this, HomeMenu.class);
				startActivity(i);
			}
		}
		menu.onClick(v);
	}

	/**
	 * create context menu
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == CONTEXT_MENU_ID)
			return menu.getIconContextMenu().createMenu("Select status");
		return super.onCreateDialog(id);
	}

	//
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent homeMenu = new Intent(this, HomeMenu.class);
			startActivity(homeMenu);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	public void clearWallpaper() {
		Intent i = new Intent(this, ComunityChatActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivity(i);
		finish();
	}

	//
	public static String getActivityName() {
		return ACTIVITY_NAME;
	}

	public LinearLayout getLayout() {
		return layout;
	}

	public void setLayout(LinearLayout layout) {
		this.layout = layout;
	}

} // end class