package dev_1.com.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

/**
 * Created by vipul on 3/5/16.
 */
public class DialogBoxFactory {
    /**
     * Create a simple dialog box with OK button and a message.
     * @param title - Title to show.
     * @param message - Message to show.
     * @param ctx - Context on which dialog will be shown.
     * @return Alert dialog created.
     */
    public static AlertDialog setDialog(String title, String message, Context ctx) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(title);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }
}
