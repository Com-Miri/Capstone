/* 

 DETECT DIFFERENT BODY PARTS USING HAAR-LIKE FEATURES

 AUTHOR: PRATEEK JOSHI

*/

#include "opencv2/objdetect/objdetect.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"

#include <iostream>
#include <stdio.h>

#include "detectBodyParts.h"

using namespace std;
using namespace cv;

#define ENABLE_FACE_DETECTION 0
#define ENABLE_UPPERBODY_DETECTION 1

int main( int argc, const char** argv )
{
    VideoCapture capture(0); // 0-내장카메라 사용
    Mat frame, prevFrame; // c::Mat가 IplImage구조체 사용하는 방법보다 빠름
    
    while( true )
    {
        // Capture the current frame from the input video stream
        capture >> frame;
        
        // Downsample the input frame for faster processing
        float downsampleFactor = 0.45;
        resize(frame, frame, Size(), downsampleFactor, downsampleFactor, INTER_NEAREST);
        
        // Apply the classifier to the frame depending on the enabled macros
        if( !frame.empty() )
        {
            if(ENABLE_FACE_DETECTION) detectFace(frame);
            if(ENABLE_UPPERBODY_DETECTION) detectUpperBody(frame);
        }
        
        else
        {
            cout << "No captured frame. Stopping!" << endl;
            break;
        }
        
        int c = waitKey(10);
        if( (char)c == 27 ) { break; }
    }
    
    capture.release();
    
    return 0;
}

