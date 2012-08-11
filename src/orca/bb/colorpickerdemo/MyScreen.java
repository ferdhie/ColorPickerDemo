package orca.bb.colorpickerdemo;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;

/**
 * A class extending the MainScreen class, which provides default standard
 * behavior for BlackBerry GUI applications.
 */
public final class MyScreen extends MainScreen
{
	int warna = Color.BLACK;
    /**
     * Creates a new MyScreen object
     */
	ColorPickerDialog dialog;
	
    public MyScreen()
    {        
        setTitle("Color Picker Demo");
        
        ButtonField button = new ButtonField("Pilih warna");
        button.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				UiApplication.getUiApplication().invokeAndWait(new Runnable() {
					public void run() {
						dialog = ColorPickerDialog.show();
					}
				});
				if (!dialog.canceled) {
					warna = dialog.resultColor;
					invalidate();
				}
			}
        });
        add(button);
        add(new LabelField("Contoh Warna") {
			protected void paint(Graphics g) {
				int col = g.getColor();
				g.setColor(warna);
				super.paint(g);
				g.setColor(col);
			}
        });
    }
}
