# Image Dump

Code to extract about 50,000 images of famous places around the world

---

>####*The code contains 3 java files:*####
>> * DriverClass.java --> Contains code to run Selenium
>> * ImageUrls.java --> TestNG class to extract image urls form browser, runs on Selenium Grid for parallel execution.
>> * IMapper.java --> Hadoop Mapper class, contains code to run the image download on Hadoop parallely.

> ---


> ####*Execution Setup:*####
>> * Need to have Hadoop setup(Single Node setup will do)
>> * Need a Selenium Grid setup (Not mandatory)
>> * Java 1.8

> ---

> ####*Execution Details:*####
>> * Run ImageUrl.java class as a TestNG file.
>>  * Generates the URLs for all the images.
>>  * Copies the generated URLs on to HDFS.
>> * Copy the input files for Map task on to HDFS. (Input files [Link](http://1drv.ms/18C7SQe "Title"))
>> * run Hadoop on class "image.mapper.IMapper" with following parameters
>>  * input
>>  * output

>> Note: output currently hardcoded

> ---

>Link to view the data downloaded: [OneDrive Link](http://1drv.ms/1BtflwI "Title")
