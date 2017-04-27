/* 

 DETECT DIFFERENT BODY PARTS USING HAAR-LIKE FEATURES

 AUTHOR: PRATEEK JOSHI

*/

#include "detectBodyParts.h"

using namespace std;
using namespace cv;

String face_cascade_name = "CascadeFiles/haarcascade_frontalface_alt.xml";
String upperbody_cascade_name = "CascadeFiles/haarcascade_mcs_upperbody.xml";

// Face detection using Haar-like features
void detectFace( Mat frame )
{
    // Load the cascade
    CascadeClassifier face_cascade;
    if( !face_cascade.load( face_cascade_name ) ) { cout << "Error loading face cascade file\n" << endl; return; };
    
    vector<Rect> faces;
    Mat frame_gray;
    
    cvtColor( frame, frame_gray, CV_BGR2GRAY );
    equalizeHist( frame_gray, frame_gray );
    
    // Detect faces
    face_cascade.detectMultiScale( frame_gray, faces, 1.1, 2, 0|CV_HAAR_SCALE_IMAGE, Size(30, 30) );
    
    for( size_t i = 0; i < faces.size(); i++ )
    {
        Point center( faces[i].x + faces[i].width*0.5, faces[i].y + faces[i].height*0.5 );
        ellipse( frame, center, Size( faces[i].width*0.5, faces[i].height*0.5), 0, 0, 360, Scalar( 255, 255, 255 ), 4, 8, 0 );
    }
    
    imshow( "Face Detection", frame );
}

// Upper body detection using Haar-like features
void detectUpperBody( Mat frame )
{
    // Load the cascade
    CascadeClassifier upperbody_cascade;
    if( !upperbody_cascade.load( upperbody_cascade_name ) ) { cout << "Error loading upper body cascade file\n" << endl; return; };
    
    vector<Rect> upperbody;
    Mat frame_gray;
    
    cvtColor( frame, frame_gray, CV_BGR2GRAY );
    equalizeHist( frame_gray, frame_gray );
    
    // Detect faces
    upperbody_cascade.detectMultiScale( frame_gray, upperbody, 1.1, 2, 0|CV_HAAR_SCALE_IMAGE, Size(100, 100) );
    
    for( size_t i = 0; i < upperbody.size(); i++ )
    {
        Rect temp = upperbody[i];
        temp.y += 100;
        rectangle(frame, temp, Scalar(255,255,255), 4, 8);
    }
    
    imshow( "Upper Body Detection", frame );
}