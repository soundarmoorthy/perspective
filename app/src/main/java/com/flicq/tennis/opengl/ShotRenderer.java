

package com.flicq.tennis.opengl;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Environment;

import com.flicq.tennis.ISystemComponent;
import com.flicq.tennis.SystemState;


public class ShotRenderer implements GLSurfaceView.Renderer, ISystemComponent {

    Shot shot;
    Grid grid;
    Axis axis;
    Helper helper;
    int initialScreenRotation;
    int width, height;
    private float[] rotationDegrees = {0.0f, 90.0f, 180.0f, 270.0f};

    // The set[] is a single dimensional array, with every "successive 7 elements" defining
    // a plot information. ax, ay, az, q0, q1, q2, q3
    public ShotRenderer(int initialScreenRotation, float[] set, int mode) {
        this.initialScreenRotation = initialScreenRotation;
        this.set  = set;
        setMode(mode);
        shot = new Shot();
        grid = new Grid();
        axis = new Axis();
        helper = new Helper();
        cameraAngleX = -45;
        cameraAngleY = 45;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //Setup
        gl.glClearColor(1.0f, 1.0f, 1.0f, 0.1f);
        gl.glClearDepthf(1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if(height == 0) {
            height = 1;
        }
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        float ratio = (float)width / height;
        GLU.gluPerspective(gl, 60.0f, ratio, 0.1f, 30.0f);
        gl.glViewport(0,0,width, height);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        this.width=width;
        this.height=height;
    }

    public float idealZ = -6.0f;
    final float matrix[]={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    private int animation=0;
    
    private void  renderFusionData(GL10 gl, float[] data, int frame, int mode){

    	int count=data.length/7;
    	float x=0,y=0,z=0;
    	float vx=0,vy=0,vz=0;
    	float[] pointData = new float[count*6*2]; 
    	for (int i=0;i<count;i++ )
    	{
	    	gl.glPushMatrix();
	    	
	    	float q0 = data[i*7+3];
	    	float q1 = data[i*7+4];
	    	float q2 = data[i*7+5];
	    	float q3 = data[i*7+6];
	        
	        float x2 = q0 * q0;
	    	float y2 = q1 * q1;
	    	float z2 = q2 * q2;
	    	float xy = q0 * q1;
	    	float xz = q0 * q2;
	    	float yz = q1 * q2;
	    	float wx = q3 * q0;
	    	float wy = q3 * q1;
	    	float wz = q3 * q2;
	        
	        matrix[0] = 1.0f - 2.0f * (y2 + z2);
	        matrix[1] = 2.0f * (xy - wz);
	        matrix[2] = 2.0f * (xz + wy);
	        matrix[3] = 0.0f;
	        
	        matrix[4] = 2.0f * (xy + wz);
	        matrix[5] = 1.0f - 2.0f * (x2 + z2);
	        matrix[6] = 2.0f * (yz - wx);
	        matrix[7] = 0.0f;
	        
	        matrix[8] = 2.0f * (xz - wy);
	        matrix[9] = 2.0f * (yz + wx);
	        matrix[10] = 1.0f - 2.0f * (x2 + y2);
	        matrix[11] = 0.0f;
	    	
	        switch(mode){
	        	case 0:{
	        		x=data[i*7+0];
	    	        y=data[i*7+1];
	    	        z=data[i*7+2];
	        		break;
	        	}
	        	case 1:{
	    	        vx+= data[i*7+0];
	    	    	vy+= data[i*7+1];
	    	    	vz+= data[i*7+2];
	    	    	
	    	    	float friction = 0.8f;
	    	    	vx*=friction;
	    	    	vy*=friction;
	    	    	vz*=friction;
	    	    	
	    	    	x+=vx*0.01f;
	    	    	y+=vy*0.01f;
	    	    	z+=vz*0.01f;
	        		break;
	        	}
	        	case 2:{
	        		
	        		float ax=data[i*7+0];
	    	        float ay=data[i*7+1];
	    	        float az=data[i*7+2];
	    	        
	    	        float qax = ax* matrix[0]+ay*matrix[4]+az*matrix[8];
	    	        float qay = ax* matrix[1]+ay*matrix[5]+az*matrix[9];
	    	        float qaz = ax* matrix[2]+ay*matrix[6]+az*matrix[10];
	    	        
	    	        ax=qax; ay=qay; az=qaz;
	    	    	vx+= ax;
	    	    	vy+= ay;
	    	    	vz+= az;
	    	    	float friction = 0.8f;
	    	    	vx*=friction;
	    	    	vy*=friction;
	    	    	vz*=friction;
	    	    	
	    	    	x+=vx*0.01f;
	    	    	y+=vy*0.01f;
	    	    	z+=vz*0.01f;
	    	    	
	        		break;
	        	}
	        }
	        
	        
	    	pointData[i*6+0]=x;
	    	pointData[i*6+1]=y;
	    	pointData[i*6+2]=z;
	    	pointData[i*6+3]=x+matrix[0];
	    	pointData[i*6+4]=y+matrix[1];
	    	pointData[i*6+5]=z+matrix[2];
	        matrix[12] = x; 
	        matrix[13] = y;
	        matrix[14] = z; 
	        matrix[15] = 1.0f;
	        
	        gl.glMultMatrixf(matrix, 0);
	        if(i==frame){
	        	shot.draw(gl, true);
	        }
	        gl.glPopMatrix();

	        
	        if(i==frame){
	        	helper.draw(gl, x, y, z);
	        }
    	}
    	if(frame==-1)
    	{
	    	gl.glEnable(GL10.GL_BLEND);
	    	gl.glDisable(GL10.GL_DEPTH_TEST);
	    	gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
	    	gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
	        ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(pointData.length * 4);
	        vertexByteBuffer.order(ByteOrder.nativeOrder());
	        FloatBuffer vertexFloatBuffer = vertexByteBuffer.asFloatBuffer();
	        vertexFloatBuffer.put(pointData);
	        vertexFloatBuffer.position(0);
	        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexFloatBuffer);
	        gl.glColor4f(0.5f, 0.7f, 1.0f, 0.5f);
	        gl.glEnable( GL10.GL_POLYGON_OFFSET_FILL );
	        gl.glPolygonOffset( 1f, 1f );
	        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, count);
	        gl.glDisable( GL10.GL_POLYGON_OFFSET_FILL );  
	        gl.glEnable(GL10.GL_DEPTH_TEST);
	        gl.glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
	        gl.glDisable(GL10.GL_BLEND);
	        gl.glDrawArrays(GL10.GL_LINES, 0, count);
	        
	        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    	}
    }
    @Override
    public void onDrawFrame(GL10 gl) {
    	cameraAngleX +=deltaX;
    	cameraAngleY +=deltaY;
    	deltaX=0;
    	deltaY=0;
        gl.glClear(GL10.GL_DEPTH_BUFFER_BIT | GL10.GL_COLOR_BUFFER_BIT);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glTranslatef(0.0f,0.0f, idealZ);
        gl.glRotatef(cameraAngleY, 1, 0, 0);
        gl.glRotatef(cameraAngleX, 0, 1, 0);
        
        gl.glRotatef(rotationDegrees[this.initialScreenRotation], 0, 0, 1);  // portrait/landscape rotation
        
        grid.draw(gl);
        axis.draw(gl);

        if(animation_use){
        	int i=(animation)%(set.length/7);
        	if(animation_play)
        		animation++;
            renderFusionData(gl, set, i, mode);
        }else
        	renderFusionData(gl, set, -1, mode);
        
        if (this.screenshot_request) {
        	take_screenshot(gl);
            this.screenshot_request = false;
        }
        

    }
    private void take_screenshot(GL10 gl){
    	int screenshotSize = this.width * this.height;
        ByteBuffer bb = ByteBuffer.allocateDirect(screenshotSize * 4);
        bb.order(ByteOrder.nativeOrder());
        gl.glReadPixels(0, 0, width, height, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, bb);
        int pixelsBuffer[] = new int[screenshotSize];
        bb.asIntBuffer().get(pixelsBuffer);
        bb = null;

        for (int i = 0; i < screenshotSize; ++i) {
            // The alpha and green channels' positions are preserved while the red and blue are swapped
            pixelsBuffer[i] = ((pixelsBuffer[i] & 0xff00ff00)) | ((pixelsBuffer[i] & 0x000000ff) << 16) | ((pixelsBuffer[i] & 0x00ff0000) >> 16);
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixelsBuffer, screenshotSize-width, -width, 0, 0, width, height);
        // TODO Auto-generated method stub
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/fusion_screenshots");    
        myDir.mkdirs();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date now = new Date();
        String fileName = formatter.format(now) + ".jpg";
        File file = new File (myDir, fileName);
        if (file.exists ()) file.delete (); 
        try {
               FileOutputStream out = new FileOutputStream(file);
               bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out); // ERROR 341 LINE
               out.flush();
               out.close();
        } catch (Exception e) {
               e.printStackTrace();
        }
    }
    int ai;
    public synchronized void computeRotationVectorFromQuaternion() {
        float theta = (float) Math.acos(q0);
        a = 2 * theta; //For some reason q0 is q0/2
        if (q0 == 1.0f) {
            x = y = z = (float) 0.0;
        } else {
            float sinTheta = (float) Math.sin(theta);
            x = q1 / sinTheta;
            y = q2 / sinTheta;
            z = q3 / sinTheta;
        }
        a = a * degreesPerRadian;
    }

    static public final float degreesPerRadian = (float) (180.0f / 3.14159f);
    
    private void setMode(int i) {
		mode = i;
		if(mode<0)
			mode=0;
		if(mode>3)
			mode=3;
		
	}

    public int length()
    {
        return set.length/7;
    }

    float q0,q1,q2,q3;
    float a,x,y,z;
	public float deltaX;
	public float deltaY;
	private int cameraAngleX;
	private int cameraAngleY;
    
	public boolean screenshot_request = false;
	public boolean animation_use = false;
	public boolean animation_play = true;
	private int mode = 1;
	
	//Rotation Quaternions
    /*
    * The data is in the following format. x, y, z, q0, q1, q2, q3
    * */
     private float set[];

    @Override
    public void SystemStateChanged(SystemState oldState, SystemState newState) {

        if (newState == SystemState.CAPTURE) {
            animation_use = false;
        } else if (newState == SystemState.RENDER) {
            animation_use = true;
        } else if (newState == SystemState.STOPPED) {
            animation_use = false;
        }
    }
}
