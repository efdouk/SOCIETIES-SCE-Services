﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using Coding4Fun.Kinect.Wpf;
using Coding4Fun.Kinect;
using Coding4Fun.Kinect.Wpf.Controls;
using System.Reflection;
using System.IO;
using System.Threading;
using System.Runtime.InteropServices;
using System.Net;
using Microsoft.Kinect;
using System.Collections;
using System.Net.Sockets;
using CoreAudioApi;

namespace MyTvUI
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        #region variables
        //user variables
        String userID;
        int currentChannel = 0;
        Boolean currentlyMuted = true;

        //socket variables
        SocketClient socketClient;
        SocketServer socketServer;
        Boolean commsInitialised = false;

        //window variables
        bool closing = false;

        //button variables
        ImageBrush channelBg_deselected;
        ImageBrush channelBg_selected;
        ImageBrush offBg_deselected;
        ImageBrush offBg_selected;
        ImageBrush muteBg_deselected;
        ImageBrush muteBg_selected;
        ImageBrush unmuteBg_deselected;
        ImageBrush unmuteBg_selected;

        //console variables
        FileStream ostrm;
        StreamWriter writer;
        TextWriter oldOut = Console.Out;

        //variables used to detect hand over hover button area
        private static double _topBoundary;
        private static double _bottomBoundary;
        private static double _leftBoundary;
        private static double _rightBoundary;
        private static double _itemLeft;
        private static double _itemTop;

        //skeleton tracking variables
        const int skeletonCount = 6;
        Skeleton[] allSkeletons = new Skeleton[skeletonCount];

        //Channel constants
        private static String channel0 = "http://www.macs.hw.ac.uk/~ceesmm1/societies/mytv/channels/splashScreen.html";
        private static String channel1 = "http://www.macs.hw.ac.uk/~ceesmm1/societies/mytv/channels/channel1.html";
        private static String channel2 = "http://www.macs.hw.ac.uk/~ceesmm1/societies/mytv/channels/channel2.html";
        private static String channel3 = "http://www.macs.hw.ac.uk/~ceesmm1/societies/mytv/channels/channel3.html";
        private static String channel4 = "http://www.macs.hw.ac.uk/~ceesmm1/societies/mytv/channels/channel4.html";

        //activity feed variables
        //ArrayList activities;

        #endregion variables

        #region window
        public MainWindow()
        {
            //redirect console output
            try
            {
                String userProfile = System.Environment.GetEnvironmentVariable("USERPROFILE");
                String directory = userProfile + @"\societies\myTvLogs.txt";
                ostrm = new FileStream(directory, FileMode.Create, FileAccess.Write);
                writer = new StreamWriter(ostrm);
                writer.AutoFlush = true;
                Console.SetOut(writer);
            }
            catch (Exception e)
            {
                Console.WriteLine("Cannot open logs.txt for writing");
                Console.WriteLine(e.Message);
                //return;
            }

            try
            {
                //initialise GUI
                //using (StreamWriter writer = new StreamWriter("logs.txt", true))
                //{
                //    writer.WriteLine("Initialising GUI");
                //}
                Console.WriteLine("Initialising GUI");
                InitializeComponent();
                channel1HoverRegion.Click += new RoutedEventHandler(channel1HoverRegion_Click);
                channel2HoverRegion.Click += new RoutedEventHandler(channel2HoverRegion_Click);
                channel3HoverRegion.Click += new RoutedEventHandler(channel3HoverRegion_Click);
                channel4HoverRegion.Click += new RoutedEventHandler(channel4HoverRegion_Click);
                offHoverRegion.Click += new RoutedEventHandler(offHoverRegion_Click);
                volumeUpHoverRegion.Click += new RoutedEventHandler(volumeUpHoverRegion_Click);
                volumeDownHoverRegion.Click += new RoutedEventHandler(volumeDownHoverRegion_Click);
                exitHoverRegion.Click += new RoutedEventHandler(exitHoverRegion_Click);
                tvBrowser.Navigated += new NavigatedEventHandler(tvBrowser_Navigated);

                //load button images
                channelBg_deselected = new ImageBrush(getImageSourceFromResource("MyTvUI", "Images/channel_background.png"));
                channelBg_selected = new ImageBrush(getImageSourceFromResource("MyTvUI", "Images/channel_background_selected.png"));
                offBg_deselected = new ImageBrush(getImageSourceFromResource("MyTvUI", "Images/off_button.png")); 
                offBg_selected = new ImageBrush(getImageSourceFromResource("MyTvUI", "Images/off_button_selected.png"));
                muteBg_deselected = new ImageBrush(getImageSourceFromResource("MyTvUI", "Images/volume_down.png"));
                muteBg_selected = new ImageBrush(getImageSourceFromResource("MyTvUI", "Images/volume_down_selected.png"));
                unmuteBg_deselected = new ImageBrush(getImageSourceFromResource("MyTvUI", "Images/volume_up.png"));
                unmuteBg_selected = new ImageBrush(getImageSourceFromResource("MyTvUI", "Images/volume_up_selected.png"));

                //initialise GUI settings
                //channel = 0
                offButton.Fill = offBg_selected;
                //mute = true
                volumeDown.Fill = muteBg_selected;

                //initialise socket server to listen for service client connections
                Console.WriteLine("Initialising SocketServer and SocketClient");
                if(initialiseSocketServer() && initialiseSocketClient())
                {
                    commsInitialised = true;
                    //get preferences
                    Console.WriteLine("Initialising personalisable parameters");
                    this.userID = socketClient.getUserID();
                    //if EMMA -> getPreferences
                    //if ARTHUR -> getUserIntent
                    //if(this.userID.Equals("emma.societies.local.macs.hw.ac.uk"))
                    //{
                        Console.WriteLine("Getting preferences for user: "+userID);
                        initialisePreferences();
                    //}else if(this.userID.Equals("arthur.societies.local.macs.hw.ac.uk"))
                    //{
                      //  Console.WriteLine("Getting intent for user: "+userID);
                      //  initialiseIntent();
                    //}
                    
                }

                //initialise activity feeds
                ArrayList activities = new ArrayList();
                listBox1.ItemsSource = activities;
                ActivityFeedManager afMgr = new ActivityFeedManager(activities);
            }
            catch (Exception e)
            {
                Console.WriteLine(e.ToString());
            }
        }

        //when window loaded
        private void Window_Loaded(object sender, RoutedEventArgs e)
        {
            //add sensor change listener to the kinect sensor chooser
            kinectSensorChooser1.KinectSensorChanged += new DependencyPropertyChangedEventHandler(kinectSensorChooser1_KinectSensorChanged);
        }

        //close window
        private void Window_Closing(object sender, System.ComponentModel.CancelEventArgs e)
        {
            Console.WriteLine("Window closing called");

            if (commsInitialised)
            {
                Console.WriteLine("Send GUI closing message to service client");
                String response = socketClient.sendMessage(
                           "START_MSG\n" +
                           "GUI_STOPPED\n" +
                           "END_MSG\n");
                if (response.Contains("RECEIVED"))
                {
                    Console.WriteLine("Service client received shutdown message");
                }
            }

            Console.WriteLine("Stopping kinect");
            closing = true;
            StopKinect(kinectSensorChooser1.Kinect);

            Console.WriteLine("Stopping sockets");
            //stop sockets
            if (commsInitialised)
            {
                socketClient.disconnect();
                socketServer.stopSocketServer();
            }

            Console.WriteLine("Flushing logs");
            if (writer != null)
            {
                Console.SetOut(oldOut);
                writer.Close();
                ostrm.Close();
            }
        }
        #endregion window

        #region serviceactions
        private Boolean setChannel1()
        {
            Console.WriteLine("Setting channel to 1");
            tvBrowser.Navigate(channel1);
            channel1Button.Fill = channelBg_selected;
            channel2Button.Fill = channelBg_deselected;
            channel3Button.Fill = channelBg_deselected;
            channel4Button.Fill = channelBg_deselected;
            offButton.Fill = offBg_deselected;
            if (commsInitialised)
            {
                Console.WriteLine("Sending channel 1 action to UAM");
                String response = socketClient.sendMessage(
                "START_MSG\n" +
                "USER_ACTION\n" +
                "channel\n" +
                "1\n" +
                "END_MSG\n");
                if (response.Contains("RECEIVED"))
                {
                    Console.WriteLine("UAM received channel 1 action");
                    //set channel button backgrounds
                    currentChannel = 1;
                }
            }
            return true;
        }       

        private Boolean setChannel2()
        {
            Console.WriteLine("Setting channel to 2");
            tvBrowser.Navigate(channel2);
            channel1Button.Fill = channelBg_deselected;
            channel2Button.Fill = channelBg_selected;
            channel3Button.Fill = channelBg_deselected;
            channel4Button.Fill = channelBg_deselected;
            offButton.Fill = offBg_deselected;
            if (commsInitialised)
            {
                Console.WriteLine("Sending channel 2 action to UAM");
                String response = socketClient.sendMessage(
                "START_MSG\n" +
                "USER_ACTION\n" +
                "channel\n" +
                "2\n" +
                "END_MSG\n");
                if (response.Contains("RECEIVED"))
                {
                    Console.WriteLine("UAM received channel 2 action");
                    //set channel button backgrounds
                    currentChannel = 2;
                }
            }
            return true;
        }

        private Boolean setChannel3()
        {
            Console.WriteLine("Setting channel to 3");
            tvBrowser.Navigate(channel3);
            channel1Button.Fill = channelBg_deselected;
            channel2Button.Fill = channelBg_deselected;
            channel3Button.Fill = channelBg_selected;
            channel4Button.Fill = channelBg_deselected;
            offButton.Fill = offBg_deselected;
            if (commsInitialised)
            {
                Console.WriteLine("Sending channel 3 action to UAM");
                String response = socketClient.sendMessage(
                "START_MSG\n" +
                "USER_ACTION\n" +
                "channel\n" +
                "3\n" +
                "END_MSG\n");
                if (response.Contains("RECEIVED"))
                {
                    Console.WriteLine("UAM received channel 3 action");
                    //set channel button backgrounds
                    currentChannel = 3;
                }
            }
            return true;
        }

        private Boolean setChannel4()
        {
            Console.WriteLine("Setting channel to 4");
            tvBrowser.Navigate(channel4);
            channel1Button.Fill = channelBg_deselected;
            channel2Button.Fill = channelBg_deselected;
            channel3Button.Fill = channelBg_deselected;
            channel4Button.Fill = channelBg_selected;
            offButton.Fill = offBg_deselected;
            if (commsInitialised)
            {
                Console.WriteLine("Sending channel 4 action to UAM");
                String response = socketClient.sendMessage(
               "START_MSG\n" +
               "USER_ACTION\n" +
               "channel\n" +
               "4\n" +
               "END_MSG\n");
                if (response.Contains("RECEIVED"))
                {
                    Console.WriteLine("UAM received channel 4 action");
                    //set channel button backgrounds
                    currentChannel = 4;
                }
            }
            return true;
        }

        private Boolean setChannel0()
        {
            Console.WriteLine("Setting channel to 0");
            tvBrowser.Navigate(channel0);
            channel1Button.Fill = channelBg_deselected;
            channel2Button.Fill = channelBg_deselected;
            channel3Button.Fill = channelBg_deselected;
            channel4Button.Fill = channelBg_deselected;
            offButton.Fill = offBg_selected;
            if (commsInitialised)
            {
                Console.WriteLine("Sending channel 0 action to UAM");
                String response = socketClient.sendMessage(
                "START_MSG\n" +
                "USER_ACTION\n" +
                "channel\n" +
                "0\n" +
                "END_MSG\n");
                if (response.Contains("RECEIVED"))
                {
                    Console.WriteLine("UAM received channel 0 action");
                    //set channel button backgrounds
                    currentChannel = 0;
                }
            }
            return true;
        }

        private Boolean setDefaultChannel()
        {
            Console.WriteLine("Setting channel to default - 0");
            tvBrowser.Navigate(channel0);
            channel1Button.Fill = channelBg_deselected;
            channel2Button.Fill = channelBg_deselected;
            channel3Button.Fill = channelBg_deselected;
            channel4Button.Fill = channelBg_deselected;
            offButton.Fill = offBg_selected;
            currentChannel = 0;
            return true;
        }

        private Boolean setDefaultVolume()
        {
            Console.WriteLine("Setting muted to default - true");
            MMDeviceEnumerator devEnum = new MMDeviceEnumerator();
            MMDevice defaultDevice = devEnum.GetDefaultAudioEndpoint(EDataFlow.eRender, ERole.eMultimedia);
            defaultDevice.AudioEndpointVolume.Mute = true;
            volumeDown.Fill = muteBg_selected;
            volumeUp.Fill = unmuteBg_deselected;
            currentlyMuted = true;
            return true;
        }

        private Boolean setMute()
        {
            Console.WriteLine("Setting muted");
            //mute volume
            MMDeviceEnumerator devEnum = new MMDeviceEnumerator();
            MMDevice defaultDevice = devEnum.GetDefaultAudioEndpoint(EDataFlow.eRender, ERole.eMultimedia);
            defaultDevice.AudioEndpointVolume.Mute = true;

            volumeDown.Fill = muteBg_selected;
            volumeUp.Fill = unmuteBg_deselected;

            if (commsInitialised)
            {
                Console.WriteLine("Sending muted action to UAM");
                String response = socketClient.sendMessage(
               "START_MSG\n" +
               "USER_ACTION\n" +
               "muted\n" +
               "true\n" +
               "END_MSG\n");
                if (response.Contains("RECEIVED"))
                {
                    Console.WriteLine("UAM received muted action");
                    //set mute button backgrounds
                    currentlyMuted = true;
                }
            }
            return true;
        }

        private Boolean setUnMute()
        {
            Console.WriteLine("Setting unmuted");
            //unmute volume
            MMDeviceEnumerator devEnum = new MMDeviceEnumerator();
            MMDevice defaultDevice = devEnum.GetDefaultAudioEndpoint(EDataFlow.eRender, ERole.eMultimedia);
            defaultDevice.AudioEndpointVolume.Mute = false;

            volumeDown.Fill = muteBg_deselected;
            volumeUp.Fill = unmuteBg_selected;

            if (commsInitialised)
            {
                Console.WriteLine("Sending unmuted action to UAM");
                String response = socketClient.sendMessage(
                "START_MSG\n" +
                "USER_ACTION\n" +
                "muted\n" +
                "false\n" +
                "END_MSG\n");
                if (response.Contains("RECEIVED"))
                {
                    Console.WriteLine("UAM received unmuted action");
                    //set mute button backgrounds
                    currentlyMuted = false;
                }
            }
            return true;
        }

        private void initialisePreferences()
        {
            //set channel
            Console.WriteLine("Requesting channel preference");
            String prefRequest = "START_MSG\n" +
                    "CHANNEL_PREFERENCE_REQUEST\n" +
                    "END_MSG\n";

            String channelPref = socketClient.sendMessage(prefRequest).Trim();
            Console.WriteLine("Got channel preference: [" + channelPref +"]");
            if (channelPref.Equals("1"))
            {
                Console.WriteLine("Personalising to channel 1");
                setChannel1();
            }
            else if (channelPref.Equals("2"))
            {
                Console.WriteLine("Personalising to channel 2");
                setChannel2();
            }
            else if (channelPref.Equals("3"))
            {
                Console.WriteLine("Personalising to channel 3");
                setChannel3();
            }
            else if (channelPref.Equals("4"))
            {
                Console.WriteLine("Personalising to channel 4");
                setChannel4();
            }
            else if (channelPref.Equals("0"))
            {
                Console.WriteLine("Personalising to channel 0");
                setChannel0();
            }
            else  //default channel is 0
            {
                setDefaultChannel();
            }
            //MessageBox.Show("Got channel preference");

            //set muted
            Console.WriteLine("Requesting mute preference");
            String muteRequest = "START_MSG\n" +
                    "MUTED_PREFERENCE_REQUEST\n" +
                    "END_MSG\n";

            String mutedPref = socketClient.sendMessage(muteRequest).Trim();
            Console.WriteLine("Got mute preference: [" + mutedPref+"]");
            if (mutedPref.Equals("false"))
            {
                //unmute tv
                Console.WriteLine("Personalising volume to unmuted");
                setUnMute();
            }
            else if (mutedPref.Equals("true"))
            {
                Console.WriteLine("Personalising volume to muted");
                setMute();
            }
            else  //default state is muted
            {
                setDefaultVolume();
            }
            //MessageBox.Show("Got muted preference");
        }

        private void initialiseIntent()
        {
            //set channel
            Console.WriteLine("Requesting channel intent");
            String intentRequest = "START_MSG\n" +
                    "CHANNEL_INTENT_REQUEST\n" +
                    "END_MSG\n";

            String channelIntent = socketClient.sendMessage(intentRequest);
            Console.WriteLine("Got channel intent :"+channelIntent);
            if (channelIntent.Equals("1"))
            {
                setChannel1();
            }
            else if (channelIntent.Equals("2"))
            {
                setChannel2();
            }
            else if (channelIntent.Equals("3"))
            {
                setChannel3();
            }
            else if (channelIntent.Equals("4"))
            {
                setChannel4();
            }
            else  //default channel is 0
            {
                setChannel0();
            }

            //set muted
            Console.WriteLine("Requesting mute preference");
            String muteRequest = "START_MSG\n" +
                    "MUTED_INTENT_REQUEST\n" +
                    "END_MSG\n";

            String mutedIntent = socketClient.sendMessage(muteRequest);
            Console.WriteLine("Got muted intent: " + mutedIntent);
            if (mutedIntent.Equals("false"))
            {
                //unmute tv
                setUnMute();
            }
            else  //default state is muted
            {
                //mute tv
                setMute();
            }
        }

        #endregion serviceactions

        /*
         * Perhaps won't be used if dynamic personalisation not applied
         */
        #region preference updates
        //public int setChannelParameter(int channel)
        //{
        //    int result;
        //    switch (channel)
        //    {
        //        case 0: 
        //            tvBrowser.Navigate(channel0);
        //            result = 0;
        //            break;
        //        case 1: 
        //            tvBrowser.Navigate(channel1);
        //            result = 1;
        //            break;
        //        case 2: 
        //            tvBrowser.Navigate(channel2);
        //            result = 2;
        //            break;
        //        case 3: 
        //            tvBrowser.Navigate(channel3);
        //            result = 3;
        //            break;
        //        case 4: 
        //            tvBrowser.Navigate(channel4);
        //            result = 4;
        //            break;
        //        default:
        //            tvBrowser.Navigate(channel0);
        //            result = 0;
        //            break;
        //    }
        //    return result;
        //}

        //public Boolean setMutedParameter(Boolean muted)
        //{
        //    Boolean result;
        //    if (muted)
        //    {
        //        //mute volume
        //        result = true;
        //    }
        //    else
        //    {
        //        //unmute volume
        //        result = false;
        //    }
        //    return result;
        //}
        #endregion preference updates

        #region sockets
        private Boolean initialiseSocketServer()
        {
            try
            {
                socketServer = new SocketServer(this);
                Thread serverThread = new Thread(new ThreadStart(socketServer.run));
                serverThread.Start();
            }
            catch (Exception e)
            {
                Console.WriteLine("Error initialising SocketServer");
                Console.WriteLine(e.ToString());
                return false;
            }
            return true;
        }

        private Boolean initialiseSocketClient()
        {
            socketClient = new SocketClient();
            if (socketClient.getSessionParameters())
            {
                userID = socketClient.getUserID();
                Console.WriteLine("Received user identity: " + userID);

                    //send handshake message with GUI IP address
                    String myIP = this.getLocalIPAddress();
                    if (myIP != null)
                    {
                        Console.WriteLine("Starting handshake");
                        Console.WriteLine("Sending service client my local IP address: " + myIP);
                        if(socketClient.connect())
                        {
                            String response = socketClient.sendMessage(
                            "START_MSG\n" +
                            "GUI_STARTED\n" +
                            myIP+"\n" +
                            "END_MSG\n");
                            if(response.Contains("RECEIVED"))
                            {
                                Console.WriteLine("Handshake complete");
                                return true;
                            }
                            else
                            {
                                Console.WriteLine("Handshake failed");
                            }
                        }
                        else{
                            Console.WriteLine("Could not connect to service client");
                        }
                    }
                    else
                    {
                        Console.WriteLine("Error - could not get IP address of local machine");
                    } 
                }
            else
            {
               Console.WriteLine("Error - could not get session parameters - userID, endpoint IP and port");
            }
            return false;
        }
        #endregion sockets
        
        #region kinect
        //listener for kinect sensor change events
        void kinectSensorChooser1_KinectSensorChanged(object sender, DependencyPropertyChangedEventArgs e)
        {
            KinectSensor oldSensor = (KinectSensor)e.OldValue;
            StopKinect(oldSensor);

            KinectSensor newSensor = (KinectSensor)e.NewValue;
            newSensor.ColorStream.Enable(ColorImageFormat.RgbResolution640x480Fps30);
            newSensor.DepthStream.Enable(DepthImageFormat.Resolution640x480Fps30);
            newSensor.SkeletonStream.Enable();
            newSensor.AllFramesReady += new EventHandler<AllFramesReadyEventArgs>(_sensor_AllFramesReady);
            try
            {
                newSensor.Start();
            }
            catch (System.IO.IOException)
            {
                kinectSensorChooser1.AppConflictOccurred();
            }
        }

        //stop connect sensor
        void StopKinect(KinectSensor sensor)
        {
            if (sensor != null)
            {
                sensor.Stop();
                sensor.AudioSource.Stop();
            }
        }

        void _sensor_AllFramesReady(object sender, AllFramesReadyEventArgs e)
        {
            if (closing)
            {
                return;
            }

            Skeleton first = GetFirstSkeleton(e);

            if (first == null)
            {
                return;
            }

            GetCameraPoint(first, e);

            //ScalePosition(leftEllipse, first.Joints[JointType.HandLeft]);
            ScalePosition(rightEllipse, first.Joints[JointType.HandRight]);

            if (currentChannel != 1)
            {
                CheckHoverButton(channel1HoverRegion, rightEllipse);
            }
            if (currentChannel != 2)
            {
                CheckHoverButton(channel2HoverRegion, rightEllipse);
            }
            if (currentChannel != 3)
            {
                CheckHoverButton(channel3HoverRegion, rightEllipse);
            }
            if (currentChannel != 4)
            {
                CheckHoverButton(channel4HoverRegion, rightEllipse);
            }
            if (currentChannel != 0)
            {
                CheckHoverButton(offHoverRegion, rightEllipse);
            }
            if (currentlyMuted)
            {
                CheckHoverButton(volumeUpHoverRegion, rightEllipse);
            }
            if (!currentlyMuted)
            {
                CheckHoverButton(volumeDownHoverRegion, rightEllipse);
            }
            CheckHoverButton(exitHoverRegion, rightEllipse);
        }
        #endregion kinect
        
        #region skeleton
        private Skeleton GetFirstSkeleton(AllFramesReadyEventArgs e)
        {
            using (SkeletonFrame skeletonFrameData = e.OpenSkeletonFrame())
            {
                if (skeletonFrameData == null)
                {
                    return null;
                }

                skeletonFrameData.CopySkeletonDataTo(allSkeletons);

                Skeleton first = (from s in allSkeletons
                                  where s.TrackingState == SkeletonTrackingState.Tracked
                                  select s).FirstOrDefault();

                return first;
            }
        }

        private void GetCameraPoint(Skeleton first, AllFramesReadyEventArgs e)
        {
            using (DepthImageFrame depth = e.OpenDepthImageFrame())
            {
                if (depth == null ||
                    kinectSensorChooser1.Kinect == null)
                {
                    return;
                }

                //get right hand depth from skeleton
                DepthImagePoint rightDepthPoint =
                    depth.MapFromSkeletonPoint(first.Joints[JointType.HandRight].Position);

                //get right hand colour from depth
                ColorImagePoint rightColorPoint =
                    depth.MapToColorImagePoint(rightDepthPoint.X, rightDepthPoint.Y,
                    ColorImageFormat.RgbResolution640x480Fps30);

                CameraPosition(rightEllipse, rightColorPoint);
            }
        }

        private void ScalePosition(FrameworkElement element, Joint joint)
        {
            Joint scaledJoint = joint.ScaleTo(1340, 700, .3f, .3f);

            Canvas.SetLeft(element, scaledJoint.Position.X);
            Canvas.SetTop(element, scaledJoint.Position.Y);
        }

        private void CameraPosition(FrameworkElement element, ColorImagePoint point)
        {
            Canvas.SetLeft(element, point.X - element.Width / 2);
            Canvas.SetTop(element, point.Y - element.Height / 2);
        }
        #endregion skeleton

        #region hoverbutton
        //check to see if right hand ellipse is in hover button region
        private void CheckHoverButton(HoverButton hoverButtonRegion, Ellipse ellipse)
        {
            if (IsPointInRegion(hoverButtonRegion, ellipse))
            {
                hoverButtonRegion.Hovering();
            }
            else
            {
                hoverButtonRegion.Release();
            }
        }

        private bool IsPointInRegion(FrameworkElement region, FrameworkElement point)
        {
            FindValues(region, point);

            if (_itemTop < _topBoundary || _bottomBoundary < _itemTop)
            {
                //Midpoint of target is outside of top or bottom
                return false;
            }

            if (_itemLeft < _leftBoundary || _rightBoundary < _itemLeft)
            {
                //Midpoint of target is outside of left or right
                return false;
            }

            return true;
        }

        private static void FindValues(FrameworkElement region, FrameworkElement point)
        {
            var containerTopLeft = region.PointToScreen(new Point());
            var itemTopLeft = point.PointToScreen(new Point());

            _topBoundary = containerTopLeft.Y;
            _bottomBoundary = _topBoundary + region.ActualHeight;
            _leftBoundary = containerTopLeft.X;
            _rightBoundary = _leftBoundary + region.ActualWidth;

            //use midpoint of item (width or height divided by 2)
            _itemLeft = itemTopLeft.X + (point.ActualWidth / 2);
            _itemTop = itemTopLeft.Y + (point.ActualHeight / 2);
        }

        //listener for channel1 hover button click events
        void channel1HoverRegion_Click(object sender, RoutedEventArgs e)
        {
            setChannel1();
        }

        //listener for channel2 hover button click events
        void channel2HoverRegion_Click(object sender, RoutedEventArgs e)
        {
            setChannel2();
        }

        //listener for channel3 hover button click events
        void channel3HoverRegion_Click(object sender, RoutedEventArgs e)
        {
            setChannel3();   
        }

        void channel4HoverRegion_Click(object sender, RoutedEventArgs e)
        {
            setChannel4();
        }

        void offHoverRegion_Click(object sender, RoutedEventArgs e)
        {
            setChannel0();
        }

        void volumeDownHoverRegion_Click(object sender, RoutedEventArgs e)
        {
            setMute();
        }

        void volumeUpHoverRegion_Click(object sender, RoutedEventArgs e)
        {
            setUnMute();
        }

        void exitHoverRegion_Click(object sender, RoutedEventArgs e)
        {
            mytvWindow.Close();
        }

        static internal ImageSource getImageSourceFromResource(string psAssemblyName, string psResourceName)
        {
            Uri oUri = new Uri("pack://application:,,,/" + psAssemblyName + ";component/" + psResourceName, UriKind.RelativeOrAbsolute);
            return BitmapFrame.Create(oUri);
        }

        #endregion hoverbutton
        
        #region additional
        private void tvBrowser_WindowLoaded(object sender, RoutedEventArgs e)
        {
            //tvBrowser.Navigate("http://www.macs.hw.ac.uk/~ceesmm1/societies/mytv/channels/splashScreen.html");
        }

        void tvBrowser_Navigated(object sender, NavigationEventArgs e)
        {
            SetSilent(tvBrowser, true);
        }

        //Code to supress Java Script errors in web browser
        public static void SetSilent(WebBrowser browser, bool silent)
        {
            if (browser == null)
                throw new ArgumentNullException("browser");

            // get an IWebBrowser2 from the document
            IOleServiceProvider sp = browser.Document as IOleServiceProvider;
            if (sp != null)
            {
                Guid IID_IWebBrowserApp = new Guid("0002DF05-0000-0000-C000-000000000046");
                Guid IID_IWebBrowser2 = new Guid("D30C1661-CDAF-11d0-8A3E-00C04FC9E26E");

                object webBrowser;
                sp.QueryService(ref IID_IWebBrowserApp, ref IID_IWebBrowser2, out webBrowser);
                if (webBrowser != null)
                {
                    webBrowser.GetType().InvokeMember("Silent", BindingFlags.Instance | BindingFlags.Public | BindingFlags.PutDispProperty, null, webBrowser, new object[] { silent });
                }
            }
        }

        [ComImport, Guid("6D5140C1-7436-11CE-8034-00AA006009FA"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
        private interface IOleServiceProvider
        {
            [PreserveSig]
            int QueryService([In] ref Guid guidService, [In] ref Guid riid, [MarshalAs(UnmanagedType.IDispatch)] out object ppvObject);
        }

        private String getLocalIPAddress()
        {
            string localIP = null;
            IPHostEntry host = Dns.GetHostEntry(Dns.GetHostName());
            foreach (IPAddress ip in host.AddressList)
            {
                if (ip.AddressFamily == AddressFamily.InterNetwork)
                {
                    localIP = ip.ToString();
                    break;
                }
            }
            return localIP;
        }

        private void mytvWindow_Closed(object sender, EventArgs e)
        {
            Console.WriteLine("Window closed called");
        }
    }

        #endregion additional
}
