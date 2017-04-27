#include <opencv2/opencv.hpp>
#include <iostream>

using namespace std;
using namespace cv;

/** Function Headers */
void detectAndDisplay(Mat frame);

/** Global variables */
String upper_body_cascade_name = "path\\to\\haarcascade_upperbody.xml";
CascadeClassifier upper_body_cascade;
string window_name = "Capture - Upper Body detection";
RNG rng(12345);

/** @function main */
int main(int argc, const char** argv)
{
    VideoCapture capture(1);
    Mat frame;

    //-- 1. Load the cascades
    if (!upper_body_cascade.load(upper_body_cascade_name)){ printf("--(!)Error loading\n"); return -1; };

    //-- 2. Read the video stream
    if (capture.isOpened())
    {
        while (true)
        {
            capture >> frame;

            //-- 3. Apply the classifier to the frame
            if (!frame.empty())
            {
                detectAndDisplay(frame);
            }
            else
            {
                printf(" --(!) No captured frame -- Break!"); break;
            }

            int c = waitKey(10);
            if ((char)c == 'c') { break; }
        }
    }
    return 0;
}

/** @function detectAndDisplay */
void detectAndDisplay(Mat frame)
{
    std::vector<Rect> bodies;
    Mat frame_gray;

    cvtColor(frame, frame_gray, CV_BGR2GRAY);
    equalizeHist(frame_gray, frame_gray);

    //-- Detect faces
    upper_body_cascade.detectMultiScale(frame_gray, bodies, 1.1, 2, 0 | CV_HAAR_SCALE_IMAGE, Size(30, 30));

    for (size_t i = 0; i < bodies.size(); i++)
    {
        rectangle(frame, bodies[i], Scalar(255, 0, 255));
    }
    //-- Show what you got
    imshow(window_name, frame);
}