package orca.bb.colorpickerdemo;

import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.TouchEvent;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class ColorPickerDialog extends PopupScreen implements FieldChangeListener {
	
	MyColorField colorfield;
	LumSlider lumslider;
	int resultColor = Color.WHITE;
	ResultColorField result;
	boolean canceled = true;
	double hue,sat,lum;
	
	static final int DEFAULT_HEIGHT = 150;
	static final int DEFAULT_SLIDER_WIDTH = 50;
	
	public ColorPickerDialog() {
		super( new VerticalFieldManager(), DEFAULT_CLOSE );

		HorizontalFieldManager hfm2 = new HorizontalFieldManager();
		add(hfm2);

		colorfield =new MyColorField();
		lumslider = new LumSlider();
		hfm2.add( colorfield );
		hfm2.add( lumslider );

		result = new ResultColorField();
		add(result);
		result.setMargin(5,5,5,5);
		
		ButtonField ok, cancel;
		HorizontalFieldManager hfm = new HorizontalFieldManager(Field.FIELD_HCENTER);
		hfm.add( ok = new ButtonField("Select", ButtonField.CONSUME_CLICK) );
		hfm.add( cancel = new ButtonField("Cancel", ButtonField.CONSUME_CLICK) );
		ok.setChangeListener(this);
		cancel.setChangeListener(this);
		
		add(hfm);
		lum = 0.5;
	}
	
	protected void update(double h, double s, double l) {
		hue=h;
		sat=s;
		lum=l;
		resultColor = HSL_TO_RGB(h,s,l);
		invalidate();
	}
	
	protected void updateHueSat(double hue, double sat) {
		update(hue,sat,lum);
	}
	
	protected void updateLuminance(double lum) {
		update(hue,sat,lum);
	}

	public void fieldChanged(Field field, int context) {
		if (field instanceof ButtonField) {
			String text = ((ButtonField)field).getLabel();
			if ("Select".equals(text)) {
				canceled = false;
			} else {
				canceled = true;
			}
			close();
		}
	}
	
	class ResultColorField extends Field {

		protected void layout(int width, int height) {
			setExtent( width, DEFAULT_SLIDER_WIDTH );
		}

		protected void paint(Graphics g) {
			
			int col = g.getColor();
			g.setColor(resultColor);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(0x666666);
			g.drawRect(0, 0, getWidth(), getHeight());
			g.setColor(col);

		}
	}
	
	class MyColorField extends Field {
		static final double INIT_LUM = .5;
		int[] rgb;
		int rgbHeight;
		int rgbWidth;
		static final int BORDER_WIDTH = 1;
		private double fixedLum;
		private int noSatFixedR, noSatFixedG, noSatFixedB;
		boolean isFocused;
		boolean _pressed = false;
		private int cursorX, cursorY;
		
		public MyColorField() {
			super(FOCUSABLE);
			fixedLum = INIT_LUM;
			noSatFixedR = (int) (fixedLum * 255);
			noSatFixedG = (int) (fixedLum * 255);
			noSatFixedB = (int) (fixedLum * 255);
		}

		/*
		protected void updateLuminance(double lum) {
			fixedLum = lum;
			noSatFixedR = (int) (fixedLum * 255);
			noSatFixedG = (int) (fixedLum * 255);
			noSatFixedB = (int) (fixedLum * 255);
			setDimension( getWidth(), getHeight() );
			invalidate();
		}
		*/
		
		protected void layout(int width, int height) {
			int h = DEFAULT_HEIGHT;
			int w = width - (DEFAULT_SLIDER_WIDTH + 10);
			setExtent(w, h);
			setDimension(w,h);
			update();
		}
		
		protected void setDimension(int width, int height) {
			rgbHeight = height - BORDER_WIDTH * 2;
			rgbWidth = width - BORDER_WIDTH * 2;
			
			if (rgb != null && rgb.length == rgbHeight * rgbWidth)
				return;
			
			rgb = new int[rgbHeight * rgbWidth];
			int yIndexArray;
			double a, b;
			double[] tempH = new double[rgbWidth];
			for (int _x = 0; _x < rgbWidth; ++_x)
				tempH[_x] = (double) _x / rgbWidth;
			double[] tempS = new double[rgbHeight];
			
			for (int _y = 0; _y < rgbHeight; ++_y)
				tempS[_y] = 1 - (double) _y / rgbHeight;
			
			for (int _y = 0; _y < rgbHeight; ++_y) {
				yIndexArray = _y * rgbWidth;
				if (fixedLum < 0.5)
					b = fixedLum * (1 + tempS[_y]);
				else
					b = (fixedLum + tempS[_y]) - (tempS[_y] * fixedLum);
				a = 2 * fixedLum - b;
				for (int _x = 0; _x < rgbWidth; ++_x) {
					rgb[yIndexArray + _x] = HSL_TO_RGB(tempH[_x], tempS[_y], a, b);
					if (rgb[yIndexArray + _x] == resultColor) {
						cursorX = _x;
						cursorY = _y;
					}
				}
			}
		}
		
	    protected void onFocus( int direction ) {
	        isFocused = true;
	        invalidate();
	        super.onFocus( direction );
	    }
	    
	    protected void onUnfocus() {
	    	isFocused = false;
	        invalidate();
	        super.onUnfocus();
	    }

		protected void paint(Graphics g) {
			int orgColor = g.getColor();
			if (isFocused || _pressed)
				g.setColor(0xFFFFFF);
			else
				g.setColor(0x666666);
			
			g.drawRect(0, 0, getWidth(), getHeight());
			g.drawARGB(rgb, 0, rgbWidth, BORDER_WIDTH, BORDER_WIDTH, rgbWidth, rgbHeight);
			
			g.setColor(Color.WHITE);
			g.drawRect(cursorX-5, cursorY-5, 10, 10);
			g.setColor(Color.BLACK);
			g.drawRect(cursorX-4, cursorY-4, 8, 8);
			
			g.setColor(orgColor);
		}
		
		protected void update() {
			double hue = (cursorX - BORDER_WIDTH) / (double) rgbWidth;
			double sat = (1 - (cursorY - BORDER_WIDTH) / (double) rgbHeight);
			updateHueSat(hue, sat);
		}
		
	    protected boolean navigationMovement( int dx, int dy, int status, int time ) {
	        if( _pressed ) {
	        	cursorX += (dx*4);
	        	if (cursorX > rgbWidth) cursorX = rgbWidth-1;
	        	if (cursorX < 0) cursorX = 0;
	        	cursorY += (dy*4);
	        	if (cursorY > rgbHeight) cursorY = rgbHeight-1;
	        	if (cursorY < 0) cursorY = 0;
	        	invalidate();
	        	update();
	            return true;
	        }
	        return super.navigationMovement( dx, dy, status, time);
	    }

		protected boolean navigationClick(int status, int time) {
			togglePressed();
			invalidate();
			update();
			return true;
		}

		public int HSL_TO_RGB(double H, double S, double a, double b) {
			int R, G, B;
			if (S == 0) // HSL from 0 to 1
			{
				R = noSatFixedR; // RGB results from 0 to 255
				G = noSatFixedG;
				B = noSatFixedB;
			} 
			else
			{
				R = roundInt(255 * Hue_2_RGB(a, b, H + (1 / 3d)));
				G = roundInt(255 * Hue_2_RGB(a, b, H));
				B = roundInt(255 * Hue_2_RGB(a, b, H - (1 / 3d)));
			}
			return 0xFF000000 | (R << 16) | (G << 8) | B;
		}
		
	    protected boolean invokeAction( int action ) {
	        if( action == ACTION_INVOKE ) {
	            togglePressed();
	            return true;
	        }
	        return false;
	    }

	    protected boolean keyChar( char key, int status, int time ) {
	        if( key == Characters.SPACE || key == Characters.ENTER ) {
	            togglePressed();
	            return true;
	        }
	        return super.keyChar(key, status, time);
	    }

	    protected boolean trackwheelClick( int status, int time ) {
	        togglePressed();
	        return true;
	    }

	    private void togglePressed() {
	        _pressed = !_pressed;
	        invalidate();
	    }
	    
	    protected boolean touchEvent( TouchEvent message ) {
	        int event = message.getEvent();
	        switch( event ) {
	            
	            case TouchEvent.CLICK:
	            case TouchEvent.DOWN:
	                // If we currently have the focus, we still get told about a click in a different part of the screen
	                if( touchEventOutOfBounds( message ) ) {
	                    return false;
	                }
	                // fall through
	                
	            case TouchEvent.MOVE:
	                _pressed = true;
	                setValueByTouchPosition( message.getX( 1 ), message.getY(1) );
	                return true;
	                
	            case TouchEvent.UNCLICK:
	            case TouchEvent.UP:
	                _pressed = false;
	                invalidate();
	                return true;
	                
	            default:
	                return false;
	        }
	    }
	    
	    private boolean touchEventOutOfBounds( TouchEvent message ) {
	        int x = message.getX( 1 );
	        int y = message.getY( 1 );
	        return ( x < 0 || y < 0 || x > getWidth() || y > getHeight() );
	    }
	        
	    private void setValueByTouchPosition( int x, int y ) {
        	cursorX = x;
        	if (cursorX > rgbWidth) cursorX = rgbWidth-1;
        	if (cursorX < 0) cursorX = 0;
        	cursorY = y;
        	if (cursorY > rgbHeight) cursorY = rgbHeight-1;
        	if (cursorY < 0) cursorY = 0;
	        invalidate();
	        update();
	    }
	}
	
	class LumSlider extends Field {
		static final int BORDER_WIDTH = 1;
		private int pointerX, pointerY;
		private double hue, sat, lum;
		private int[] rgb = null;
		private int rgbX, rgbY, rgbH, rgbW;
		boolean isFocused;
		boolean _pressed = false;
		
		public LumSlider() {
			this(.5);
		}

		public LumSlider(double lum) {
			super(FOCUSABLE);
			hue = 0;
			sat = 0;
			this.lum = lum;
			pointerY = DEFAULT_HEIGHT-BORDER_WIDTH;
		}
		
		protected void layout(int width, int height) {
			setExtent( DEFAULT_SLIDER_WIDTH, DEFAULT_HEIGHT );
			setRgbPosDimen();
		}

		protected void paint(Graphics g) {
			int orgColor = g.getColor();
			if (isFocused || _pressed)
				g.setColor(0xFFFFFF);
			else
				g.setColor(0x666666);
			
			g.drawRect(0, 0, getWidth(), getHeight());
			g.drawARGB(rgb, 0, rgbW, BORDER_WIDTH, BORDER_WIDTH, rgbW, rgbH);
			
			g.setColor(Color.WHITE);
			g.drawRect(0, pointerY-5, rgbW, 10);
			g.setColor(Color.BLACK);
			g.drawRect(1, pointerY-4, rgbW-2, 8);
			
			g.setColor(orgColor);
		}
		
		/*
		public void updateHueSat(double hue, double sat) {
			this.hue = hue;
			this.sat = sat;
			updateRGB();
			invalidate();
		}
		*/
	 
		private void updateRGB() {
			int yIndexArray;
			double lum;
			for (int _y = 0; _y < rgbH; _y++) {
				yIndexArray = _y * rgbW;
				lum = 1 - (double) _y / rgbH;
				for (int _x = 0; _x < rgbW; _x++) {
					rgb[yIndexArray + _x] = HSL_TO_RGB(hue, sat, lum);
				}
			}
		}

		private void setRgbPosDimen() {
			int h = getHeight();
			if (h <= 0) h = DEFAULT_HEIGHT;
			
			int w = getWidth();
			if (w <= 0) h = DEFAULT_SLIDER_WIDTH;
			
			if (rgb != null && rgb.length == (rgbW*rgbH))
				return;
			
			rgbY = BORDER_WIDTH;
			rgbH = h - (BORDER_WIDTH * 2);
			rgbW = w - (BORDER_WIDTH * 2);
			rgbX = BORDER_WIDTH;
			if (rgbH > 0 && rgbW > 0)
				rgb = new int[rgbH * rgbW];
			pointerX = 0;
			pointerY = (int) (rgbY + (lum * rgbH) / 2);
			updateRGB();
		}
	 		
	    protected boolean navigationMovement( int dx, int dy, int status, int time ) {
	        if( _pressed ) {
	        	pointerY += (dy*4);
	        	if (pointerY > rgbH) pointerY = rgbH-1;
	        	if (pointerY < 0) pointerY = 0;
        		
	        	lum = ((double)pointerY/rgbH);
        		if (lum < 0) lum=0;
        		if (lum > 1) lum=1;
	        	
	        	invalidate();
	        	updateLuminance( 1-lum );
	        	Log.info( "LUM="+lum );
	            return true;
	        }
	        return super.navigationMovement( dx, dy, status, time);
	    }

		protected boolean navigationClick(int status, int time) {
			togglePressed();
			invalidate();
			return true;
		}

	    protected boolean invokeAction( int action ) {
	        if( action == ACTION_INVOKE ) {
	            togglePressed();
	            return true;
	        }
	        return false;
	    }

	    protected boolean keyChar( char key, int status, int time ) {
	        if( key == Characters.SPACE || key == Characters.ENTER ) {
	            togglePressed();
	            return true;
	        }
	        return super.keyChar(key, status, time);
	    }

	    protected boolean trackwheelClick( int status, int time ) {
	        togglePressed();
	        return true;
	    }

	    private void togglePressed() {
	        _pressed = !_pressed;
	        invalidate();
	    }
	    
	    protected boolean touchEvent( TouchEvent message ) {
	        int event = message.getEvent();
	        switch( event ) {
	            
	            case TouchEvent.CLICK:
	            case TouchEvent.DOWN:
	                // If we currently have the focus, we still get told about a click in a different part of the screen
	                if( touchEventOutOfBounds( message ) ) {
	                    return false;
	                }
	                // fall through
	                
	            case TouchEvent.MOVE:
	                _pressed = true;
	                setValueByTouchPosition( message.getX( 1 ), message.getY(1) );
	                return true;
	                
	            case TouchEvent.UNCLICK:
	            case TouchEvent.UP:
	                _pressed = false;
	                invalidate();
	                return true;
	                
	            default:
	                return false;
	        }
	    }
	    
	    private boolean touchEventOutOfBounds( TouchEvent message ) {
	        int x = message.getX( 1 );
	        int y = message.getY( 1 );
	        return ( x < 0 || y < 0 || x > getWidth() || y > getHeight() );
	    }
	        
	    private void setValueByTouchPosition( int x, int y ) {
        	pointerY = y;
        	if (pointerY > rgbH) pointerY = rgbH-1;
        	if (pointerY < 0) pointerY = 0;
    		lum = (double) y / rgbH;
    		updateLuminance(1 - lum);
	        invalidate();
	    }
	    
	    protected void onFocus( int direction ) {
	        isFocused = true;
	        invalidate();
	        super.onFocus( direction );
	    }
	    
	    protected void onUnfocus() {
	    	isFocused = false;
	        invalidate();
	        super.onUnfocus();
	    }

	}

	public static double Hue_2_RGB(double v1, double v2, double vH) // Function Hue_2_RGB
	{
		if (vH < 0)
			vH += 1;
		if (vH > 1)
			vH -= 1;
		if ((6 * vH) < 1)
			return (v1 + (v2 - v1) * 6 * vH);
		if ((2 * vH) < 1)
			return (v2);
		if ((3 * vH) < 2)
			return (v1 + (v2 - v1) * ((2 / 3d) - vH) * 6);
		return (v1);
	}

	public static int HSL_TO_RGB(double H, double S, double L) {
		int R, G, B;
		double a, b;
		if (S == 0) // HSL from 0 to 1
		{
			R = (int) (L * 255); // RGB results from 0 to 255
			G = (int) (L * 255);
			B = (int) (L * 255);
		} else {
			if (L < 0.5)
				b = L * (1 + S);
			else
				b = (L + S) - (S * L);
 
			a = 2 * L - b;
 
			R = roundInt(255 * Hue_2_RGB(a, b, H + (1 / 3d)));
			G = roundInt(255 * Hue_2_RGB(a, b, H));
			B = roundInt(255 * Hue_2_RGB(a, b, H - (1 / 3d)));
		}
		return 0xFF000000 | (R << 16) | (G << 8) | B;
	}
	
	private static int roundInt(double a) {
		return (int) (a+0.5);
	}
	
	public static ColorPickerDialog show() {
		final ColorPickerDialog dialog = new ColorPickerDialog();
		UiApplication.getUiApplication().invokeAndWait(new Runnable() {
			public void run() {
				UiApplication.getUiApplication().pushModalScreen(dialog);
			}
		});
		return dialog;
	}
}
