#Author: David Walshe
#Date: 02/10/2017
#Description: This script is used to format the XML output of the Java logger class
#             The logger class should be set up to save to a file with a filehandler to make use of this script.

use strict;
use warnings;

my $inFile = $ARGV[0];
my $outFile = $ARGV[1];
print("$ARGV[0]\n");
print("$ARGV[1]\n");

open(my $readFile, $inFile);
open(my $writeFile, ">$outFile");


my $sequence = "";
my $class = "";
my $message = "";
my $method = "";
my $thread = "";
my $dateTime = "";
my $logMsg = "";
my $date = "";
my $time = "";
my $level = "";

while(<$readFile>)
{
    if(/<sequence>/)
    {
        $sequence = extractXMLTag($_);
    }
    elsif(/<class>/)
    {
        $class = extractXMLTag($_);
    }
    elsif(/<thread>/)
    {
        $thread = extractXMLTag($_);
    }
    elsif(/<method>/)
    {
        $method = extractXMLTag($_);
    }
    elsif(/<message>/)
    {
        $message = extractXMLTag($_);
    }
    elsif(/<level>/)
    {
        $level = extractXMLTag($_);
    }
    elsif(/<date>/)
    {
        $dateTime = extractXMLTag($_);
        my @arr = split("T", $dateTime);
        $date = $arr[0];
        $time = $arr[1];
    }
    elsif(/<\/record>/)
    {

        $logMsg = "Sequence ID:\t$sequence\nLog Time:\t\t$time $date\nCalled From:\t$class->$method\nThread ID:\t\t$thread\nLevel:\t\t\t$level\nMessage:\t\t$message\n-----------------------------------------------------------------------------------------\n";
        $logMsg = replace($logMsg, "&gt;", ">");
        $logMsg = replace($logMsg, "&lt;", "<");
        print $writeFile $logMsg;
        $sequence = "";
        $class = "";
        $message = "";
        $method = "";
        $thread = "";
        $logMsg = "";
        $dateTime = "";
        $date = "";
        $time = "";
    }
}

sub extractXMLTag
{
    my $string = $_[0];
    my $startIndex = index($string, ">") + 1;
    my $endIndex = lastIndex($string, "<");
    my $tag = substr $string, $startIndex, $endIndex-$startIndex;
    return $tag;
}

sub lastIndex
{
    my $indx = 0;
    my $temp = 0;
    my $string = $_[0];
    my $char = $_[1];
    while($temp != -1)
    {
        $temp = index($string, $char, $temp+1);
        if($temp != -1)
        {
            $indx = $temp;
        }
    }
    return $indx;
}

sub replace
{
    my $string = $_[0];
    my $replaceStr = $_[1];
    my $newStr = $_[2];
    my $len = length($replaceStr);
    my $i = 0;
    while(index($string, $replaceStr) != -1)
    {
        $i = index($string, $replaceStr);
        my $preStr = substr($string, 0, $i);
        my $postStr = substr($string, $i+$len);
        $string = $preStr . $newStr . $postStr;
    }
    return $string;
}