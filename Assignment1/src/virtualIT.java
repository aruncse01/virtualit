/*
 * Copyright 2010-2013 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import java.lang.System;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.io.*;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.Activity;
import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.CreateAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.CreateLaunchConfigurationRequest;
import com.amazonaws.services.autoscaling.model.DeleteAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsResult;
import com.amazonaws.services.autoscaling.model.DescribeLaunchConfigurationsRequest;
import com.amazonaws.services.autoscaling.model.DescribeLaunchConfigurationsResult;
import com.amazonaws.services.autoscaling.model.DescribeScalingActivitiesRequest;
import com.amazonaws.services.autoscaling.model.DescribeScalingActivitiesResult;
import com.amazonaws.services.autoscaling.model.LaunchConfiguration;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyRequest;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyResult;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.cloudwatch.model.PutMetricAlarmRequest;
import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodb.model.CreateTableRequest;
import com.amazonaws.services.dynamodb.model.CreateTableResult;
import com.amazonaws.services.dynamodb.model.KeySchema;
import com.amazonaws.services.dynamodb.model.KeySchemaElement;
import com.amazonaws.services.dynamodb.model.ProvisionedThroughput;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AllocateAddressResult;
import com.amazonaws.services.ec2.model.AssociateAddressRequest;
import com.amazonaws.services.ec2.model.AttachVolumeRequest;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.CreateImageRequest;
import com.amazonaws.services.ec2.model.CreateImageResult;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.CreateSnapshotRequest;
import com.amazonaws.services.ec2.model.CreateSnapshotResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.CreateVolumeRequest;
import com.amazonaws.services.ec2.model.CreateVolumeResult;
import com.amazonaws.services.ec2.model.DeleteVolumeRequest;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeKeyPairsResult;
import com.amazonaws.services.ec2.model.DescribeSnapshotsRequest;
import com.amazonaws.services.ec2.model.DescribeSnapshotsResult;
import com.amazonaws.services.ec2.model.DescribeVolumeAttributeResult;
import com.amazonaws.services.ec2.model.DescribeVolumesResult;
import com.amazonaws.services.ec2.model.DetachVolumeRequest;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.KeyPair;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Snapshot;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.Volume;
import com.amazonaws.services.ec2.model.VolumeAttachment;
import com.amazonaws.services.opsworks.model.StopInstanceRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.DomainMetadataRequest;
import com.amazonaws.services.simpledb.model.DomainMetadataResult;
import com.amazonaws.services.simpledb.model.ListDomainsRequest;
import com.amazonaws.services.simpledb.model.ListDomainsResult;



import java.util.*;

public  class virtualIT
{
	public static final String securityGroupName="raghusecgroup05";
	public static final String securityGroupDescription="raghu security group05";
	public static final String keyName="raghukeypair05";
	public static final String stopTime="17";
	public static final String autoScalingGroupName="VirtAutoScale";
	public static final String LAUNCH_CONFIGURATION="VirlaunchautoScal";
	private static String BUSY_ALARM_NAME = "busy-alarm";
	private static String IDLE_ALARM_NAME = "idle-alarm";
	private static Integer EVALUATION_PERIOD = 60;
	private static String METRIC_NAME = "CPUUtilization";
	private static String NAMESPACE="AWS/EC2";
	private static Integer EC2_METRIC_SUBMIT_PERIOD = 60;
	private static String STATISTIC = "Average";
	private static Double THRESHOLD = 60d;
	private static String SCALE_UP_POLICY_NAME = "scaleup-policy";
	private static String SCALE_UP_POLICY_ARN=null;
	private static String SCALE_DOWN_POLICY_NAME = "scaledown-policy";
	private static String SCALE_DOWN_POLICY_ARN=null;
	private static int COOL_DOWN_DURATION = 120;
	private static Integer SSH_CONNECTION_TIME_OUT = 10 * 60000;// (10min)
	public static Integer IDLE_THRESHOLD_PERIOD = 120;
	private static int COMM_RETRY_WAITING_TIME = 1 * 1000;// 6s waiting
	private static int RETRY_LIMIT = 1;
	private static Integer SCALE_UP_DOWN_THRESHOLD_PERIOD = 60 * 20;
	public String currentHour;
	public static Date tempMin;
	public static String availableZone;
	static AmazonEC2      ec2;
	static AmazonS3Client s3;
	static AmazonAutoScalingClient autoScaleClient;
	static AmazonDynamoDBClient db;
	static AmazonCloudWatchClient cloudWatch;
	List<String> rootvolumeIdList = new LinkedList<String>();
	List<String> extravolumeIdList = new LinkedList<String>();
	List<String> instanceIdList = new LinkedList<String>();
	List<String> availableZones = new LinkedList<String>();
	List<Integer> userIds= new LinkedList<Integer>();
	List<String> AutoScalingGroup = new LinkedList<String>();
	Map<Integer,String> mapUserRootVol= new HashMap<Integer,String>();
	Map<Integer,String> mapUserInst =new HashMap<Integer,String>();
	Map<Integer,String> mapUserRootSnap = new HashMap<Integer,String>();
	Map<Integer,String> mapUserExtraSnap =new HashMap<Integer,String>();
	Map<Integer,String> mapUserImage = new HashMap<Integer,String>();
	Map<Integer,String> mapUserExtraVolu = new HashMap<Integer,String>();
	Map<Integer,Double> mapUserCpu=new HashMap<Integer,Double>();
	
	 //private static virtualIT instance;
	// private virtualIT()
	// {
		
	// }
	   
	  /* public static virtualIT getInstance() {
	      if(instance == null) {
	         instance = new virtualIT();
	      }
	      return instance;
	   }*/
	   
	public static void main(String[] args) throws Exception
	{
	AWSCredentials credentials = 
			new PropertiesCredentials(
       virtualIT.class.getResourceAsStream("AwsCredentials.properties"));
int intialCounter=0;
int s;
virtualIT vIT= new virtualIT();
 /*********************************************
  * 
  *  #1 Create Amazon Client object
  *  
  *********************************************/
System.out.println("#1 Create Amazon Client object");
 ec2 = new AmazonEC2Client(credentials);
db = new AmazonDynamoDBClient(credentials);
cloudWatch = new AmazonCloudWatchClient(credentials) ;
autoScaleClient= new AmazonAutoScalingClient(credentials);
vIT.deleteAutoScalingGroup(12345);
try {
	System.out.println("Starting Virtual System Admin \n");
	System.out.println("Enter no of users want to login\n");
	Scanner getInput= new Scanner(System.in);
	 s=getInput.nextInt();
	if(s!=0)
	{
		for(int j=0;j<s;j++)
		{
			System.out.println("Enter UserId:\n");
			int getUserId=getInput.nextInt();
			if(getUserId!=0)
			{
		    vIT.userIds.add(getUserId);
		    vIT.StartUpGroup(getUserId);
		    vIT.cloudWatchMonitor(getUserId);
			}
		}
		
		Calendar cal = Calendar.getInstance();
    	Date time=cal.getTime();
    	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    	
    	 vIT.currentHour=(sdf.format(Calendar.HOUR));
    	System.out.println( sdf.format(Calendar.HOUR) );
    	/****
    	 * Entering Continuous Loop
    	 */
    	for(int i=0;i< vIT.userIds.size();i++)
    	{
    		
    		int userId=vIT.userIds.get(i);
    		double cpu=vIT.mapUserCpu.get(userId);
    		tempMin=cal.getTime();
    		
    	while(vIT.currentHour.equals(stopTime)||cpu<5);
    	{ 
    		
    		  vIT.cloudWatchMonitor(userId);
    		
    		/****************
    		 * Entering Time end Group Keeps on looping 
    		 */
    		vIT.TimeEndGroup();
    	}
    	}
    	

		
		
		
	
	}
	/*********************************************
	 * 
     *  #2 Describe Availability Zones.
     *  
     *********************************************/
	/*System.out.println("#2 Describe Availability Zones.");
    DescribeAvailabilityZonesResult availabilityZonesResult = ec2.describeAvailabilityZones();
    System.out.println("You have access to " + availabilityZonesResult.getAvailabilityZones().size() +
            " Availability Zones.");*/

    /*********************************************
     * 
     *  #3 Describe Available Images
     *  
     *********************************************/
    /*System.out.println("#3 Describe Available Images");
    DescribeImagesResult dir = ec2.describeImages();
    List<Image> images = dir.getImages();
    System.out.println("You have " + images.size() + " Amazon images");*/
    
    
    /*********************************************
     *                 
     *  #4 Describe Key Pair
     *                 
     *********************************************/
   /* System.out.println("#9 Describe Key Pair");
    DescribeKeyPairsResult dkr = ec2.describeKeyPairs();
    System.out.println(dkr.toString());*/
    
    /*********************************************
     * 
     *  #5 Describe Current Instances
     *  
     *********************************************/
   /* System.out.println("#4 Describe Current Instances");
    DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
    List<Reservation> reservations = describeInstancesRequest.getReservations();
    Set<Instance> instances = new HashSet<Instance>();
    // add all instances to a Set.
    for (Reservation reservation : reservations) {
    	instances.addAll(reservation.getInstances());
    }
    
    System.out.println("You have " + instances.size() + " Amazon EC2 instance(s).");
    for (Instance ins : instances){
    	
    	// instance id
    	String instanceId = ins.getInstanceId();
    	
    	// instance state
    	InstanceState is = ins.getState();
    	System.out.println(instanceId+" "+is.getName());
   // 	System.out.println("Key for the instance  "+keyName);
    }*/
   
    			
    			
    
    
    
    
    
    
    
   // Thread.currentThread().sleep(100000);
    
    //get instanceId from the result
    
  /*  List<Instance> resultInstance = result.getReservation().getInstances();
    String createdInstanceId = null;
    for (Instance ins : resultInstance){	
    	createdInstanceId = ins.getInstanceId();
    	System.out.println("New instance has been created: "+ins.getInstanceId());
    }
    describeInstancesRequest = ec2.describeInstances();
    reservations = describeInstancesRequest.getReservations();
    int k = reservations.size();
    Reservation totalReservations = reservations.get(k-1);
    Instance getFirstInstance = totalReservations.getInstances().get(0);
   
    System.out.println("The private IP is: "+getFirstInstance.getPrivateIpAddress());
    System.out.println("The public IP is: "+getFirstInstance.getPublicIpAddress());*/
         

}catch (AmazonServiceException ase) {
    System.out.println("Caught Exception: " + ase.getMessage());
    System.out.println("Reponse Status Code: " + ase.getStatusCode());
    System.out.println("Error Code: " + ase.getErrorCode());
    System.out.println("Request ID: " + ase.getRequestId());
}

}
	private void StartUpGroup(int userId) throws Exception
	{
		/*****
		 * Starting the New Instances
		 */
		availableZone=getAvailabilityzones();
		runInstance(keyName, securityGroupName,1,availableZone);
		//createAutoScaling(userId);
		//describeCurrentInstances(userId);
		//defaultVolumeRequest(userId);
		//createVolumeRequest(userId);
		//AttachVolume(userId);
	}
	private void TimeEndGroup() throws Exception
	{
		/********
		 * Stop the instances and detach the volumes
		 */
		for(int i=0;i<userIds.size();i++)
		{
			int userId=userIds.get(i);
		detachVolume(userId);
		createImageUser(userId);
		 Thread.currentThread();
		 Thread.sleep(50000);
		groupStopInstance(userId, 3);
		 Thread.currentThread();
		 Thread.sleep(10000);
		}
	}
	private void OldLoginRunGroup(int userId) throws Exception
	{
		/*****
		 * Start from the OldAmi
		 */
		StartUsingOldAmi(userId);
	}
	private String getAvailabilityzones()
	{
		/*********************************************
		 * 
	     *  #2 Describe Availability Zones.
	     *  
	     *********************************************/
		System.out.println("#2 Describe Availability Zones.");
	    DescribeAvailabilityZonesResult availabilityZonesResult = ec2.describeAvailabilityZones();
	    List<AvailabilityZone> availableZones=availabilityZonesResult.getAvailabilityZones();
	    for(Iterator<AvailabilityZone> n =availableZones.iterator(); n.hasNext(); ) {
   		  AvailabilityZone item = n.next();
   		  this.availableZones.add(item.getZoneName());
   		  System.out.println("You have access to "+item.getZoneName()+" Availability Zones.");
	    }
	    
	   return this.availableZones.get(0).toString();
     	
	}
	private void describeAvailableImages()
	{
		/*********************************************
	     * 
	     *  #3 Describe Available Images
	     *  
	     *********************************************/
	    System.out.println("#3 Describe Available Images");
	    DescribeImagesResult dir = ec2.describeImages();
	    List<Image> images = dir.getImages();
	    System.out.println("You have " + images.size() + " Amazon images");
	}
	
	
	
	
	
	
	private void describeKeyPair()
	{
		/*********************************************
	     *                 
	     *  #4 Describe Key Pair
	     *                 
	     *********************************************/
	    System.out.println("#9 Describe Key Pair");
	    DescribeKeyPairsResult dkr = ec2.describeKeyPairs();
	    System.out.println(dkr.toString());
		
	}
	private void describeCurrentInstances(int userId) throws Exception
	{
		
		/*********************************************
	     * 
	     *  #5 Describe Current Instances
	     *  
	     *********************************************/
	    System.out.println("#4 Describe Current Instances");
	    Thread.currentThread();
		Thread.sleep(25000);
	    DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
	   
	    List<Reservation> reservations = describeInstancesRequest.getReservations();
	    Set<Instance> instances = new HashSet<Instance>();
	    // add all instances to a Set.
	    for (Reservation reservation : reservations) {
	    	instances.addAll(reservation.getInstances());
	    	reservation.getInstances();
	    }
	    
	    System.out.println("You have " + instances.size() + " Amazon EC2 instance(s).");
	    for (Instance ins : instances){
	    	
	    	// instance id
	    	String instanceId = ins.getInstanceId();
	    	InstanceState is = ins.getState();
	    	// instance state
	    	if(is.getName().equals("running"))
	    	{
	    		if(!instanceIdList.contains(instanceId)||!mapUserInst.containsValue(instanceId))
	    		{
	    	instanceIdList.add(instanceId);
	    	elasticIP( instanceId);
	    	mapUserInst.put(userId, instanceId);
	    		}
	    		else
	    		{
	    	
	    		}
	    	}
	    	System.out.println(instanceId+" "+is.getName());
	    	
	    }
	}
	
	private void elasticIP(String createdInstanceId)
	{
		
		
        
        /* START Elastic IP Address */
        
        AllocateAddressResult elasticResult = ec2.allocateAddress();
        String elasticIp = elasticResult.getPublicIp();
        System.out.println("New elastic IP: "+elasticIp);
                
        //associate
        AssociateAddressRequest aar = new AssociateAddressRequest();
        aar.setInstanceId(createdInstanceId);
        aar.setPublicIp(elasticIp);
        ec2.associateAddress(aar);
        
        
        
        /* END Elastic IP Address */
	}
	
	private void createVolumeRequest(int userId)
	{
		/*********************************************
         *  #2.1 Create a volume
         *********************************************/
      	//create a volume
		
     	CreateVolumeRequest cvr = new CreateVolumeRequest();
     	cvr
     	.withAvailabilityZone(availableZone)
     	.withSize(1);//size = 10 gigabytes
     	CreateVolumeResult volumeResult = ec2.createVolume(cvr);
     	String createdVolumeId = volumeResult.getVolume().getVolumeId();
     	System.out.println(createdVolumeId);
     	extravolumeIdList.add(createdVolumeId);
     	mapUserExtraVolu.put(userId, createdVolumeId);
	       
	}
	private void defaultVolumeRequest(int userId)
	{
		String instanceId = null;
     	String volumeId;
     	DescribeVolumesResult describeVolumeResult =ec2.describeVolumes();
     	List<Volume> volumeData=describeVolumeResult.getVolumes();
     	for(Volume item : volumeData)
     	{
     		volumeId=item.getVolumeId();
     		List<VolumeAttachment> volumeAttachment=item.getAttachments();
     		for (VolumeAttachment data:volumeAttachment)
     		{
     			instanceId=data.getInstanceId();
     		}
     		//instanceIdList.add(instanceId);
     		rootvolumeIdList.add(volumeId);
     		mapUserRootVol.put(userId,volumeId);
     	}
	}
	private void createSecurityGroup(String securityGroupName,String securityGroupDescription)
	{
		//Create security group and pass the request to amazon.
	    
	    CreateSecurityGroupRequest createSecurityGroupRequest = 
	    		new CreateSecurityGroupRequest();
	   
	    	createSecurityGroupRequest.withGroupName(securityGroupName)
	    		.withDescription(securityGroupDescription);
	    	
	    	CreateSecurityGroupResult createSecurityGroupResult = 
	    			  ec2.createSecurityGroup(createSecurityGroupRequest);
	    	
		
	}
	private void setRulesSecurityGroup(String securityGroupName)
	{
		
		    	//Add rules to your group
		    	
		    	IpPermission tcpIpPermission = 
		    			new IpPermission();
		    	IpPermission httpIpPermission = 
		    			new IpPermission();
		    	IpPermission sshIpPermission = 
		    			new IpPermission();
		    	tcpIpPermission.withIpRanges("0.0.0.0/0")
		    		            .withIpProtocol("tcp")
		    		            .withFromPort(0)
		    		            .withToPort(65535);
		    	httpIpPermission.withIpRanges("0.0.0.0/0")
		            .withIpProtocol("tcp")
		            .withFromPort(80)
		            .withToPort(80);
		    	sshIpPermission.withIpRanges("0.0.0.0/0")
		            .withIpProtocol("tcp")
		            .withFromPort(22)
		            .withToPort(22);
		    		
		    		List<IpPermission> ipPermissionList = new ArrayList<IpPermission>();
		    		ipPermissionList.add(tcpIpPermission);
		    		ipPermissionList.add(httpIpPermission);
		    		ipPermissionList.add(sshIpPermission);

		    		
		    		AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest =
		    				new AuthorizeSecurityGroupIngressRequest();
		    			    	
		    			authorizeSecurityGroupIngressRequest.withGroupName(securityGroupName)
		    			                                    .withIpPermissions(ipPermissionList);
		    			
		    	
		    			ec2.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);
	
	}
	
	private void createKeyPair(String keyName)
	{
		 //Create a new  KeyPair 	
		
		System.out.print("Creating a new Key Pair\n");
		CreateKeyPairRequest createKeyPairRequest = 
				new CreateKeyPairRequest();
				    	
			createKeyPairRequest.withKeyName(keyName);
			
			CreateKeyPairResult createKeyPairResult = 
					ec2.createKeyPair(createKeyPairRequest);
			
			System.out.println("Key Pair created\n"+"KeyName:"+createKeyPairResult.getKeyPair().getKeyName()+
					"\n");
			
			KeyPair keyPair = new KeyPair();
	    	
			keyPair = createKeyPairResult.getKeyPair();
				    	
			String privateKey = keyPair.getKeyMaterial();
			
			
			 /* get the keypair and store it in a file*/
	        
	        String fileName="d:/"+keyName+".pem";
	        File distFile = new File(fileName); 
	        BufferedReader keyReader = new BufferedReader(new StringReader(keyPair.getKeyMaterial()));
	        BufferedWriter keyWriter;
			try {
				keyWriter = new BufferedWriter(new FileWriter(distFile));
			
	        char buf[] = new char[1024];        
	        int len; 
	        while ((len = keyReader.read(buf)) != -1) { 
	                keyWriter.write(buf, 0, len); 
	        } 
	        keyWriter.flush(); 
	        keyReader.close(); 
	        keyWriter.close();
			}
	        catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	         
		
	}
	private void runInstance(String keyName,String securityGroupName,int instanceCount,String availableZone)throws Exception
	{
		/*********************************************
	     * 
	     *  #6 Create an Instance
	     *  
	     *********************************************/
	    System.out.println("#5 Create an Instance");
	    String imageId = "ami-ab844dc2";//Basic 32-bit Amazon Linux AMI
	    String instanceType="t1.micro";
	    int minInstanceCount = instanceCount; // create 1 instance
	    int maxInstanceCount = instanceCount;
	    Placement placement= new Placement(availableZone);
	    RunInstancesRequest instanceRun = new RunInstancesRequest();
	    instanceRun
	    .withImageId(imageId)
	    .withInstanceType(instanceType)
	    .withMinCount(minInstanceCount)
	    .withMaxCount(maxInstanceCount)
	    .withKeyName(keyName)
	    .withPlacement(placement)
	    .withSecurityGroups(securityGroupName);
	    
	    
	    
	    RunInstancesResult result = ec2.runInstances(instanceRun);
	    
	    Thread.currentThread();
		Thread.sleep(100000);
	    
	    
	   
	}
	
	private void AttachVolume(int userId)
	{
		/*********************************************
         *  #2.2 Attach the volume to the instance
         *********************************************/
		System.out.println("Attach the volume to the instance using userId's\n");
		String volumeId=mapUserExtraVolu.get(userId);
		String instanceId=mapUserInst.get(userId);
     	AttachVolumeRequest avr = new AttachVolumeRequest();
     	avr.setVolumeId(volumeId);
     	avr.setInstanceId(instanceId);
     	avr.setDevice("/dev/sda");
     	ec2.attachVolume(avr);
     	System.out.println("Volumeid:"+volumeId+" "+"attached to" +"Instance Id:"+instanceId+"\n");
	}

	private void detachVolume(int userId) throws Exception
	{
		/*********************************************
         *  #2.3 Detach the volume from the instance
         *********************************************/
		int i;
		String state;
		System.out.print("Detaching extra volumes from user\n");
		String volumeId=mapUserExtraVolu.get(userId);
		String instanceId=mapUserInst.get(userId);
     	DetachVolumeRequest dvr = new DetachVolumeRequest();
     	dvr.setVolumeId(volumeId);
     	dvr.setInstanceId(instanceId);
     	ec2.detachVolume(dvr);
     	System.out.println("Volumeid:"+volumeId+" "+"detached from" +"Instance Id:"+instanceId+"\n");
     	/*DeleteVolumeRequest delete=new DeleteVolumeRequest();
     	DescribeVolumesResult describeVolumeResult=ec2.describeVolumes();
     	List<Volume> volume=describeVolumeResult.getVolumes();
     	for(Volume item:volume)
     	{
     		state=item.getState();
     		while(item.getState().equals("available"))
     		{
     			if(item.getVolumeId().equals(extravolumeIdList.get(i)))
     		
         	delete.setVolumeId(extravolumeIdList.get(i));
         	ec2.deleteVolume(delete);
     		}
     	}*/
     	//DeleteVolumeRequest delete=new DeleteVolumeRequest();
     	//delete.setVolumeId(extravolumeIdList.get(i));
     	//ec2.deleteVolume(delete);
     	//DeleteVolumeRequest delete=new DeleteVolumeRequest();
     	//delete.setVolumeId(extravolumeIdList.get(i));
     	//ec2.deleteVolume(delete);
     	//mapUserExtraVolu.remove(userId);
     	
     	for(Iterator<String> n = extravolumeIdList.iterator(); n.hasNext(); ) {
     		  String item = n.next();
     		 extravolumeIdList.remove(item);
     	}
     	
     	}
	private void createExtraSnapShot(int userId) throws Exception
	{
		
		/******************************************
		 * Creating Snap Shots before detaching volume
		 */
		System.out.println("Creating Snap Shots before detaching volume\n");
		String volumeId= mapUserExtraVolu.get(userId);
		CreateSnapshotResult snapRes
	    = ec2.createSnapshot(new CreateSnapshotRequest(volumeId, "Test snapshot"+userId));
		Snapshot snap = snapRes.getSnapshot();

		System.out.println("Snapshot request sent.");
		System.out.println("Waiting for snapshot to be created");
		
		String snapState = snap.getState();
		System.out.println("snapState is " + snapState);
		
		System.out.print("Waiting for snapshot to be created");
		// Wait for the snapshot to be created
		while (snapState.equals("pending"))
		{
		    Thread.sleep(500);
		    System.out.print(".");
		    DescribeSnapshotsResult describeSnapRes 
		        = ec2.describeSnapshots(new DescribeSnapshotsRequest().withSnapshotIds(snap.getSnapshotId()));
		snapState = describeSnapRes.getSnapshots().get(0).getState();
		
		 }
		mapUserExtraSnap.put(userId,snap.getSnapshotId());
		
		 System.out.println("\nSnap shot Done.");
		 return;		
	}
	private void createRootSnapShot(int userId) throws Exception
	{
		String volumeId= mapUserRootVol.get(userId);
		CreateSnapshotResult snapRes
	    = ec2.createSnapshot(new CreateSnapshotRequest(volumeId, "Test snapshot"+userId));
		Snapshot snap = snapRes.getSnapshot();

		System.out.println("Snapshot request sent.");
		System.out.println("Waiting for snapshot to be created");
		
		String snapState = snap.getState();
		System.out.println("snapState is " + snapState);
		
		System.out.print("Waiting for snapshot to be created");
		// Wait for the snapshot to be created
		while (snapState.equals("pending"))
		{
		    Thread.sleep(500);
		    System.out.print(".");
		    DescribeSnapshotsResult describeSnapRes 
		        = ec2.describeSnapshots(new DescribeSnapshotsRequest().withSnapshotIds(snap.getSnapshotId()));
		snapState = describeSnapRes.getSnapshots().get(0).getState();
		 }
		mapUserRootSnap.put(userId,snap.getSnapshotId());
		
		 System.out.println("\nSnap shot Done.");
		 return;		
	}
	
	private void createImageUser(int userId)
	{
		String instanceId= mapUserInst.get(userId);
		CreateImageRequest cir = new CreateImageRequest();
        cir.setInstanceId(instanceId);
        cir.setName("ami-"+instanceId);
        CreateImageResult createImageResult = ec2.createImage(cir);
        String createdImageId = createImageResult.getImageId();
        System.out.println("Sent creating AMI request. AMI id="+createdImageId);
        mapUserImage.put(userId, createdImageId);
	}
	private void groupStopInstance(int userId,int n) throws InterruptedException
	{
		/*********************************************
	     * 
	     *  #8 Stop/Start an Instance
	     *  
	     *********************************************/
		System.out.print("Stop Instance");
		//List<Instance> resultInstance = result.getReservation().getInstances();
		//List<String> instanceIds = new LinkedList<String>();
		//String createdInstanceId = null;
	  //  for (Instance ins : resultInstance){	
	  //  	createdInstanceId = ins.getInstanceId();
	  //  System.out.println("#7 Stop the Instance");
	  //  instanceIds.add(createdInstanceId);  
	  //  }
		String instanceId=mapUserInst.get(userId);
		List<String> instanceIds = new LinkedList<String>();
		instanceIds.add(instanceId);
	    switch(n)
	    {
	    	case 1:
	    		//stop
	    		StopInstancesRequest stopIR = new StopInstancesRequest(instanceIds);
	    	    ec2.stopInstances(stopIR);
	    	    Thread.sleep(1000);
	    	case 2:
	    		//Start
	    		 StartInstancesRequest startIR = new StartInstancesRequest(instanceIds);
	    		    ec2.startInstances(startIR);
	    	case 3:
	    		//Terminate
	    		 System.out.println("#8 Terminate the Instance");
	    	    TerminateInstancesRequest tir = new TerminateInstancesRequest(instanceIds);
	    	    ec2.terminateInstances(tir);
	    	    Thread.sleep(10000);
	    	    mapUserInst.remove(userId);
	    } 
	    System.out.print("Stop Instance done");
	    
	}
	
	private void StartUsingOldAmi(int userId) throws Exception
	{
		/*Run instances */
		String imageId= mapUserImage.get(userId);
		System.out.println("Old Instance");
		Placement placement= new Placement(availableZone);
		RunInstancesRequest instanceRun = new RunInstancesRequest();
		  instanceRun
		 .withImageId(imageId)
		 .withInstanceType("t1.micro")
		  .withMinCount(1)
		  .withMaxCount(1)
		  .withPlacement(placement)
		  .withKeyName(keyName)
		  .withSecurityGroups(securityGroupName);  
	    RunInstancesResult result = ec2.runInstances(instanceRun);
	    describeCurrentInstances(userId);
	    AttachVolume(userId);
	}

	/*private void createDatabase()
	{
		
		String tableName = "UserDetails";

		KeySchemaElement hashKey = new KeySchemaElement().withAttributeName("UserId").withAttributeType("N");
		KeySchema ks = new KeySchema().withHashKeyElement(hashKey);

		ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput()
		  .withReadCapacityUnits(10L)
		  .withWriteCapacityUnits(10L);

		CreateTableRequest request = new CreateTableRequest()
		  .withTableName(tableName)
		  .withKeySchema(ks)
		  .withProvisionedThroughput(provisionedThroughput);

		CreateTableResult result = db.createTable(request);
	}*/
	    
	    private void cloudWatchMonitor(int userId) throws InterruptedException
	    {
	    	
			String instanceId=mapUserInst.get(userId);
			//create request message
			GetMetricStatisticsRequest statRequest = new GetMetricStatisticsRequest();
			
			//set up request message
			statRequest.setNamespace("AWS/EC2"); //namespace
			statRequest.setPeriod(60); //period of data
			ArrayList<String> stats = new ArrayList<String>();
			
			//Use one of these strings: Average, Maximum, Minimum, SampleCount, Sum 
			stats.add("Average"); 
			stats.add("Sum");
			statRequest.setStatistics(stats);
			
			//Use one of these strings: CPUUtilization, NetworkIn, NetworkOut, DiskReadBytes, DiskWriteBytes, DiskReadOperations  
			statRequest.setMetricName("CPUUtilization"); 
			
			// set time
			GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
			calendar.add(GregorianCalendar.SECOND, -1 * calendar.get(GregorianCalendar.SECOND)); // 1 second ago
			Date endTime = calendar.getTime();
			calendar.add(GregorianCalendar.MINUTE, -10); // 10 minutes ago
			Date startTime = calendar.getTime();
			statRequest.setStartTime(startTime);
			statRequest.setEndTime(endTime);
			
			//specify an instance
			ArrayList<Dimension> dimensions = new ArrayList<Dimension>();
			dimensions.add(new Dimension().withName("InstanceId").withValue(instanceId));
			statRequest.setDimensions(dimensions);
			
			//get statistics
			cloudWatch.getMetricStatistics(statRequest);
			Thread.currentThread().sleep(1000000);
			GetMetricStatisticsResult statResult = cloudWatch.getMetricStatistics(statRequest);
			 
			//display
			System.out.println(statResult.toString());
			List<Datapoint> dataList = statResult.getDatapoints();
			Double averageCPU = null;
			Date timeStamp = null;
			for (Datapoint data : dataList){
				averageCPU = data.getAverage();
				timeStamp = data.getTimestamp();
				System.out.println("Average CPU utlilization for last 10 minutes: "+averageCPU);
				System.out.println("Totl CPU utlilization for last 10 minutes: "+data.getSum());
				
				
				
			
	    }
		
			
			if(!mapUserCpu.containsKey(userId))
			{
			mapUserCpu.put(userId, averageCPU);
			}
			else 
			{
				mapUserCpu.remove(userId);
				/**************
				 * update 
				 * 
				 /
				 */
				mapUserCpu.put(userId, averageCPU);
			}
	
	
	
}
	    private static void createLaunchConfiguration(int user) {
			System.out.println("Launching Configuration");
			//
			DescribeLaunchConfigurationsRequest describeLaunchConfigurationsRequest = new DescribeLaunchConfigurationsRequest();
			describeLaunchConfigurationsRequest.withLaunchConfigurationNames(LAUNCH_CONFIGURATION);
			DescribeLaunchConfigurationsResult describeLaunchConfigurationResult = autoScaleClient
					.describeLaunchConfigurations(describeLaunchConfigurationsRequest);
			List<LaunchConfiguration> configs = describeLaunchConfigurationResult
					.getLaunchConfigurations();

			// launch config already exists
			if (configs.size() != 0)
				return;
			
			CreateLaunchConfigurationRequest createLaunchConfigurationRequest = new CreateLaunchConfigurationRequest();
			createLaunchConfigurationRequest
					.withLaunchConfigurationName(LAUNCH_CONFIGURATION)
					.withImageId("ami-ab844dc2")
					.withInstanceType("t1.micro")
					.withKeyName(keyName)
					.withSecurityGroups(securityGroupName);
			autoScaleClient.createLaunchConfiguration(createLaunchConfigurationRequest);
		}
		
	    
	    private static void  createAutoScalingGroup(int userId) throws InterruptedException {
	        System.out.println("Creating Auto Scale Group");
	        
	        CreateAutoScalingGroupRequest createAutoScalingGroupRequest = new CreateAutoScalingGroupRequest();
	         createAutoScalingGroupRequest
	                .withAutoScalingGroupName(virtualIT.autoScalingGroupName)
	                .withLaunchConfigurationName(LAUNCH_CONFIGURATION)
	                .withAvailabilityZones(availableZone).withMinSize(0)
	                 .withMaxSize(1).withDesiredCapacity(1);
	         autoScaleClient
	                .createAutoScalingGroup(createAutoScalingGroupRequest);
	    }
	    
	    
	    
	    
	    
	    private void  deleteAutoScalingGroup(int userId)
	             throws InterruptedException {
	        System.out.println("delete auto scaling group for"+ userId);
	        
	        DescribeAutoScalingGroupsRequest describeAutoScalingGroupsRequest = new DescribeAutoScalingGroupsRequest();
	         describeAutoScalingGroupsRequest.withAutoScalingGroupNames(virtualIT.autoScalingGroupName);
	        DescribeAutoScalingGroupsResult describeAutoScalingGroupResult = autoScaleClient
	                .describeAutoScalingGroups(describeAutoScalingGroupsRequest);
	         List<AutoScalingGroup> groups = describeAutoScalingGroupResult
	                .getAutoScalingGroups();
	        if (groups.size() == 0)
	            return;
	        // waiting for auto scaling activities to complete
	        //-- waitForAutoScalingActivities(user);
	        // delete the pre-existed one
	        DeleteAutoScalingGroupRequest deleteAutoScalingGroupRequest = new DeleteAutoScalingGroupRequest();
	        deleteAutoScalingGroupRequest.withAutoScalingGroupName(virtualIT
	                 .autoScalingGroupName);
	        
	        autoScaleClient.deleteAutoScalingGroup(deleteAutoScalingGroupRequest);
	        System.out.println("deleting auto scaling group done for"+
	        		 userId);
	    }
	    
	    private static void waitForAutoScalingActivities(int userId)
	            throws InterruptedException {
	        DescribeScalingActivitiesRequest describeScalingActivitiesRequest = new DescribeScalingActivitiesRequest();
	         describeScalingActivitiesRequest.withAutoScalingGroupName(virtualIT
	                 .autoScalingGroupName);

	        System.out.println("waiting for all auto scaling activites of [{}] to accomplish"+
	        		userId);
	        while (true) {
	            boolean allDone = true;
	            //logger.debug(".");
	            Thread.currentThread().sleep(COMM_RETRY_WAITING_TIME);

	             DescribeScalingActivitiesResult scalingActivitiesResult = autoScaleClient
	                    .describeScalingActivities(describeScalingActivitiesRequest);
	            for (Activity activity : scalingActivitiesResult.getActivities()) {
	                 if ("InProgress".equals(activity.getStatusCode())) {
	                    allDone = false;
	                }
	            }
	            if (allDone)
	                break;
	        }

	    }

	private static void createScalingPolicy(int userId) {
	        System.out.println("Creating Scalling Policy");
	        PutScalingPolicyRequest putScalingUpPolicyRequest = new PutScalingPolicyRequest();
	         putScalingUpPolicyRequest.withPolicyName(SCALE_UP_POLICY_NAME)
	                .withAutoScalingGroupName(virtualIT.autoScalingGroupName)
	                .withScalingAdjustment(1)
	                .withAdjustmentType("ChangeInCapacity").withCooldown(COOL_DOWN_DURATION);

	        PutScalingPolicyRequest putScalingDownPolicyRequest = new PutScalingPolicyRequest();
	        putScalingDownPolicyRequest.withPolicyName(SCALE_DOWN_POLICY_NAME)
	                .withAutoScalingGroupName(virtualIT.autoScalingGroupName)
	                 .withScalingAdjustment(-1)
	                .withAdjustmentType("ChangeInCapacity").withCooldown(COOL_DOWN_DURATION);

	        PutScalingPolicyResult scalingupResult = autoScaleClient
	                 .putScalingPolicy(putScalingUpPolicyRequest);
	        PutScalingPolicyResult scalingdownResult = autoScaleClient
	                .putScalingPolicy(putScalingDownPolicyRequest);

	        SCALE_UP_POLICY_ARN = scalingupResult.getPolicyARN();
	         SCALE_DOWN_POLICY_ARN = scalingdownResult.getPolicyARN();
	    }
	    
	    private static void createTriggers(int userId) {
	        System.out.println("Creating Trigger");
	        Dimension dimension = new Dimension();
	         dimension.setName("AutoScalingGroupName");
	        dimension.setValue(virtualIT.autoScalingGroupName);

	        PutMetricAlarmRequest putMetricBusyAlarmRequest = new PutMetricAlarmRequest();
	         putMetricBusyAlarmRequest.withAlarmName(BUSY_ALARM_NAME)
	                .withComparisonOperator("GreaterThanOrEqualToThreshold")
	                .withUnit("Seconds").withEvaluationPeriods(EVALUATION_PERIOD)
	                 .withMetricName(METRIC_NAME).withNamespace(NAMESPACE)
	                .withPeriod(EC2_METRIC_SUBMIT_PERIOD).withStatistic(STATISTIC)
	                .withThreshold(THRESHOLD).withAlarmActions(SCALE_UP_POLICY_ARN)
	                 .withDimensions(dimension);

	        PutMetricAlarmRequest putMetricIdleAlarmRequest = new PutMetricAlarmRequest();
	        putMetricIdleAlarmRequest.withAlarmName(IDLE_ALARM_NAME)
	                .withComparisonOperator("LessThanOrEqualToThreshold")
	                 .withUnit("Seconds").withEvaluationPeriods(EVALUATION_PERIOD)
	                .withMetricName(METRIC_NAME).withNamespace(NAMESPACE)
	                .withPeriod(EC2_METRIC_SUBMIT_PERIOD).withStatistic(STATISTIC)
	                 .withThreshold(THRESHOLD)
	                .withAlarmActions(SCALE_DOWN_POLICY_ARN)
	                .withDimensions(dimension);

	        cloudWatch.putMetricAlarm(putMetricBusyAlarmRequest);
	        cloudWatch.putMetricAlarm(putMetricIdleAlarmRequest);

	    }
	    
	    public static void createAutoScaling(int user) throws InterruptedException{
			createLaunchConfiguration(user);
			createAutoScalingGroup(user);
			createScalingPolicy(user);
			createTriggers(user);
	    
	
	    }
}
