#include "opencv2/objdetect/objdetect.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include <iostream>
#include <stdio.h>
#include <pthread.h>
#include "detectBodyParts.h"
using namespace std;
using namespace cv;
#define ENABLE_FACE_DETECTION 0
#define ENABLE_EYE_DETECTION 0
#define ENABLE_EAR_DETECTION 0 
#define ENABLE_MOUTH_DETECTION 0
#define ENABLE_NOSE_DETECTION 0
#define ENABLE_SMILE_DETECTION 0
#define ENABLE_LOWERBODY_DETECTION 0
#define ENABLE_UPPERBODY_DETECTION 1
#define ENABLE_FULLBODY_DETECTION 0
//Rect r;
extern Rect temp2;
char *Filename;
void TestThread();
int main( int argc, char** argv )
{
    VideoCapture capture(0); // capture video from webcam
    Mat frame, prevFrame;
    int status, status2;
   int a=0;
   pthread_t threads, threads2;
   Filename = argv[1];
   //printf("%s", Filename);
   //pthread_create(&threads, NULL, &TestThread, (void *)&a);
    while( true )
    {
        // Capture the current frame from the input video stream
        capture >> frame;
        
        // Downsample the input frame for faster processing
        float downsampleFactor = 0.25;
        resize(frame, frame, Size(), downsampleFactor, downsampleFactor, INTER_NEAREST);
        
        // Apply the classifier to the frame depending on the enabled macros
        if( !frame.empty() )
        {
            if(ENABLE_FACE_DETECTION) detectFace(frame);
            if(ENABLE_EYE_DETECTION) detectEyes(frame);
            if(ENABLE_EAR_DETECTION) detectEars(frame);
            if(ENABLE_MOUTH_DETECTION) detectMouth(frame);
            if(ENABLE_NOSE_DETECTION) detectNose(frame);
            if(ENABLE_SMILE_DETECTION) detectSmile(frame);
            if(ENABLE_LOWERBODY_DETECTION) detectLowerBody(frame);
            if(ENABLE_UPPERBODY_DETECTION) {
      detectUpperBody(frame);
      TestThread();
      //pthread_create(&threads, NULL, &TestThread, (void *)&a);
       }
            if(ENABLE_FULLBODY_DETECTION) detectFullBody(frame);
        }
        
        else
        {
            cout << "No captured frame. Stopping!" << endl;
            break;
        }
        int c = waitKey(10);
        if( (char)c == 27 ) { break; }
   //pthread_join(threads, (void **)&status);
    }
    //pthread_join(threads, (void **)&status);
    //pthread_join(threads2, (void **)&status2);
    capture.release();
    
    return 0;
}
void TestThread()
{
        int w = 1050;//1016; <-mirror//1080;
        int h = 1680;//1856; <-mirror//1920;
        Mat image(h, w, CV_8UC3);
   Mat cloth = imread(Filename);
        image = Scalar(3);
   Rect temp3;
   Mat tmpcloth;
   int clwid, clhei;
   
   temp3.x =  w/(480*1/4)*temp2.x;//w/temp2.height;
   temp3.y =  h/(640*1/4)*(temp2.y-100)+100;//h/temp2.width;
   temp3.width = w/(640*1/4)*temp2.width;
   temp3.height = h/(480*1/4)*temp2.height;
 
   if(temp3.x<=0&&temp3.y<=0&&temp3.width<=0&&temp3.height<=0){
   temp3.x=100;
   temp3.y=100;
   temp3.width=100;
   temp3.height=100;
   }
   //printf("%s", Filename);
        //printf("2.x:%d, 2.y:%d, 2.wid:%d 2.hei:%d\n", temp2.x, temp2.y, temp2.width, temp2.height);
   //printf("3.x:%d, 3.y:%d, 3.wid:%d 3.hei:%d\n", temp3.x, temp3.y, temp3.width, temp3.height);
   //rectangle(image, temp3, Scalar(255,255,255), 4, 8);
clhei=cloth.rows*temp3.width/cloth.cols;
temp3.height=clhei;
   //rectangle(image, temp3, Scalar(255,0,0), 4, 8);
   //if(temp3.x+clwid>w)
      //clwid=w-temp3.x;
   //if(temp3.y+clhei>h)
      //clhei=h-temp3.y;//printf("y error");
   resize(cloth, tmpcloth, Size(temp3.width, temp3.height));
   Mat imageROI=image(Rect(temp3.x, temp3.y, tmpcloth.cols, tmpcloth.rows));
   tmpcloth.copyTo(imageROI);
   //printf("x: %d, y: %d, wid: %d, hei: %d\n", temp3.x, temp3.y, temp3.width, temp3.height);
        //printf("cols=%d, rows=%d\n",cloth.cols, cloth.rows);
   //printf("tmpcols=%d, tmprows=%d\n", tmpcloth.cols, tmpcloth.rows);
   namedWindow("Image", WINDOW_NORMAL);
        setWindowProperty("Image", WND_PROP_FULLSCREEN, WINDOW_FULLSCREEN);
        imshow("Image", image);
}
