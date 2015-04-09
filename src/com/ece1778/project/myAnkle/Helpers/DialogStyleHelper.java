package com.ece1778.project.myAnkle.Helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.graphics.Typeface;
import android.widget.Button;
import android.widget.TextView;

// to display the dialog from another activity/fragment, call the showDialog() method on an
// object of this class (instead of show()). to retain the default button and message parameters,
// call the corresponding methods with the arguments null, -1, -1

@SuppressWarnings("unused")
public class DialogStyleHelper extends AlertDialog implements OnShowListener {

	private Context mContext;
	private AlertDialog mAlertDialog;
	
	// button parameters
	private String mButtonTextFont = "sans-serif-light"; //default text style
	private int mButtonTypefaceStyle = Typeface.NORMAL; //default type-face
	private int mButtonTextColor = getContext().getResources() //default text color
					.getColor(android.R.color.primary_text_light);
	
	// message (text) parameters
	private String mMessageTextFont = "sans-serif-light";
	private int mMessageTypefaceStyle = Typeface.NORMAL;
	private int mMessageTextColor = getContext().getResources()
					.getColor(android.R.color.primary_text_light);
		
	// constructor with the context and AlertDialog object provided
	public DialogStyleHelper(Context context, AlertDialog dialog) {
		super(context);
		
		this.mAlertDialog = dialog;
		this.mContext = context;
	}
	
	// constructor with context, theme and AlertDialog object provided
	public DialogStyleHelper(Context context, int theme, AlertDialog dialog) {
		super(context, theme);
		
		this.mAlertDialog = dialog;
		this.mContext = context;
	}	
		
	// set the button parameters such as text font, type-face style and color
	// (null, -1, -1) sets the parameters to default
	public void setDialogButtonParams(String buttonTextFont, int buttonTypefaceStyle, 
			int buttonTextColor) {
		
		// set the button's text style
		if(buttonTextFont != null) {
			this.mButtonTextFont = buttonTextFont;
		}
		
		// set the button's type-face style
		if(buttonTypefaceStyle != -1) {
			this.mButtonTypefaceStyle = buttonTypefaceStyle;
		}
		
		// set the button's text color
		if(buttonTextColor != -1) {
			this.mButtonTextColor = buttonTextColor;
		}
	}
	
	// set the message (text) parameters. (null, -1, -1) sets parameters to default
	public void setDialogMessageParams(String messageTextFont, int messageTypefaceStyle,
			int messageTextColor) {
		
		// set the text style for the message
		if(messageTextFont != null) {
			this.mMessageTextFont = messageTextFont;
		}
		
		// set the type-face style
		if(messageTypefaceStyle != -1) {
			this.mMessageTypefaceStyle = messageTypefaceStyle;
		}
		
		if(messageTextColor != -1) {
			this.mMessageTextColor = messageTextColor;
		}
	}
	
	// show the created dialog
	public void showDialog() {
		
		// register the listener (to check when the dialog is instantiated) 
		mAlertDialog.setOnShowListener(this);
		mAlertDialog.show();
	}

	// called when the alert dialog (mAlertDialog) is displayed
	@Override
	public void onShow(DialogInterface dialog) {
		
		// link the layout objects to the corresponding dialog-ui elements
		Button negativeButton = ((AlertDialog) dialog).getButton(BUTTON_NEGATIVE);
		Button neutralButton = ((AlertDialog) dialog).getButton(BUTTON_NEUTRAL);
		Button positiveButton = ((AlertDialog) dialog).getButton(BUTTON_POSITIVE);
		
		TextView dialogText = (TextView) ((AlertDialog) dialog)
				.findViewById(android.R.id.message);
		
		// set the parameters for the negative button, if exists
		if(negativeButton != null) {
			negativeButton.setTypeface(Typeface.create(mButtonTextFont, mButtonTypefaceStyle));
			negativeButton.setTextColor(mButtonTextColor);
		}
		
		// set the parameters for the neutral button, if exists
		if(neutralButton != null) {
			neutralButton.setTypeface(Typeface.create(mButtonTextFont, mButtonTypefaceStyle));
			neutralButton.setTextColor(mButtonTextColor);
		}
		
		// set the parameters for the positive button, if exists
		if(positiveButton != null) {
			positiveButton.setTypeface(Typeface.create(mButtonTextFont, mButtonTypefaceStyle));
			positiveButton.setTextColor(mButtonTextColor);
		}
		
		//set the arguments for the displayed text message
		if(dialogText != null) {
			dialogText.setTypeface(Typeface.create(mMessageTextFont, mMessageTypefaceStyle));
			dialogText.setTextColor(mMessageTextColor);
		}
	}
}
