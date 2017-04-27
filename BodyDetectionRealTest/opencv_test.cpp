#include <opencv\cv.h>

#include <opencv\highgui.h>

using namespace cv;




int main()

{

	Mat img = imread("Desert.jpg");

//Mat img = imread("C:\\Users\\Public\\Pictures\\Sample Pictures\\Desert.jpg");

	imshow("window Name", img);




	waitKey(0);




	return 0;

}
