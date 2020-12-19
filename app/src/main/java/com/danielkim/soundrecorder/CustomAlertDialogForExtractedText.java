package com.danielkim.soundrecorder;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CustomAlertDialogForExtractedText extends Dialog implements OnClickListener{
    private Button buttonOk;
    private Button buttonCopy;
    private TextView customAlertDialaogForExtractedTextBody;

    public CustomAlertDialogForExtractedText(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_alert_dialog_for_extracted_text);

        this.buttonOk = (Button) findViewById(R.id.customAlertDialaogForExtractedTextButtonOk);
        this.buttonCopy = (Button) findViewById(R.id.customAlertDialaogForExtractedTextButtonCopy);
        this.customAlertDialaogForExtractedTextBody = (TextView) findViewById(R.id.customAlertDialaogForExtractedTextBody);

        this.buttonOk.setOnClickListener(this);
        this.buttonCopy.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.customAlertDialaogForExtractedTextButtonOk:
                dismiss();
                break;

            case R.id.customAlertDialaogForExtractedTextButtonCopy:
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getContext().getResources().getString(R.string.customAlertDialogFromAudioBodyDescription), customAlertDialaogForExtractedTextBody.getText());
                clipboard.setPrimaryClip(clip);

                dismiss();

                Toast.makeText(getContext(), R.string.textCopiedToClipboard, Toast.LENGTH_LONG).show();

                break;

            default:
                break;
        }
    }

    public void setButtonCopyEnabled(boolean enabled){
        this.buttonCopy.setEnabled(enabled);
    }

    public void setText(String text) {
        this.customAlertDialaogForExtractedTextBody.setText(text);
    }

    public TextView getBodyTextView() {
        return customAlertDialaogForExtractedTextBody;
    }
}
