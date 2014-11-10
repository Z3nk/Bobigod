package BobiGod;
import robocode.*;

import static robocode.util.Utils.normalRelativeAngleDegrees;
import static robocode.util.Utils.normalRelativeAngle;
import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * ProAim - a robot by (your name here)
 */
public class BobiGod extends AdvancedRobot
{
	final double DIST_COURS=100, DIST_MOYEN=225, MOUVEMENT_LATERAL=45, MOUVEMENT_BACK=135, MINI_VITESSE=6;
	//BASE = dist cour = 100, moyen = 225, lateral = 65, back = 115, vitesse = 6
	double rand;
	long prevTime = 0;
	int randFrequencity = 20+(int)(Math.random()*30.0);
	int randMove = 1;
	int trackSens = 1;
	long trackTime = 0, prevScanTime;
	boolean turningRadar = false, targeting = false, melee=false;
	int curColor=0, colorSens=1;
	int randStat, nbRand=0, totRand=0;
	double targetRelY = 0;
	double targetRelX = 0;
		
	double targetRelPredY = 0;
	double targetRelPredX = 0;
	double targetPredDist = 0;
	
	double nextX = 0;
	double nextY = 0;
		
	double targNextX = 0;
	double targNextY = 0;
	double targetHeading = 0;
	double targetVelocity = 0;
	
	double precisionAngle = 45;
	
	public void run()
	{
		Color c= new Color(curColor, curColor, 0);
		setColors(c,c,c,c,c);
		
		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		
		while(true)
		{
			if(!melee){
				
				if(getRadarTurnRemaining() == 0)
					turningRadar = false;
			
				if(!turningRadar)
				{
					setTurnRadarRight(trackSens*90);
				}
			
				targetPredDist = 0;
			
				execute();
			}	
			else{}
		}
	}

	public void onScannedRobot(ScannedRobotEvent e)
	{
		
		if(!melee){
			randDeplacement();	
			changeColor();
		
			targeting = true;
			prevScanTime = getTime();
		
			// ciblage
		
			if(getRadarTurnRemaining() == 0)
			turningRadar = false;
			
			targetHeading = e.getHeadingRadians();
		    targetVelocity = e.getVelocity();
		
		
			targetRelY = Math.cos(normalRelativeAngle(e.getBearingRadians() + getHeadingRadians()))*e.getDistance();
			targetRelX = Math.sin(normalRelativeAngle(e.getBearingRadians() + getHeadingRadians()))*e.getDistance();
		
			targetRelPredY = targetRelY + Math.cos(e.getHeadingRadians())*e.getVelocity()*targetPredDist/Rules.getBulletSpeed(getBulletPowerFor(e));
			targetRelPredX = targetRelX + Math.sin(e.getHeadingRadians())*e.getVelocity()*targetPredDist/Rules.getBulletSpeed(getBulletPowerFor(e));
		
			targetPredDist = Math.sqrt(targetRelPredX*targetRelPredX+targetRelPredY*targetRelPredY);
		
			nextX = getX()+Math.sin(getHeadingRadians())*getVelocity();
			nextY = getY()+Math.cos(getHeadingRadians())*getVelocity();
		
			targNextX = getX()+targetRelX+Math.sin(e.getHeadingRadians())*e.getVelocity();
			targNextY = getY()+targetRelY+Math.cos(e.getHeadingRadians())*e.getVelocity();
		
			precisionAngle = Math.abs(Math.atan(getWidth()/e.getDistance()));
			if(precisionAngle > Rules.RADAR_TURN_RATE_RADIANS/2)
			precisionAngle = Rules.RADAR_TURN_RATE_RADIANS/2;
			double turnRadar = normalRelativeAngle(Math.atan2( targNextX-nextX, targNextY-nextY )+trackSens*precisionAngle/2-getRadarHeadingRadians());

			
		
		
			double turnGun = normalRelativeAngle(Math.atan2( targetRelPredX, targetRelPredY ) - getGunHeadingRadians() );
		
			if(!turningRadar)
			{
				trackSens*=-1;
				//setTurnRadarRightRadians(turnRadar);
				setTurnRadarRightRadians(normalRelativeAngle(e.getBearingRadians()+getHeadingRadians()-getRadarHeadingRadians()+(precisionAngle/2)*trackSens));
				turningRadar = true;
			}
		
			setTurnGunRightRadians( turnGun );
		
			if( Math.abs(turnGun) < precisionAngle/2 && targetPredDist != 0 )
				setFire(getBulletPowerFor(e));
		
			/*if(turnRadar > 0)
			trackSens = 1;
			else
			trackSens = -1;*/
		
			// deplacement
		
			if(getTime() > prevTime+randFrequencity)
			{
				randStat=/*4*/(int)(Math.random()*10.0);
				totRand+=randStat;
				nbRand++;
				prevTime = getTime();
				randFrequencity = 10+randStat;
				randMove*=-1;
			}
		
		
			if(getX() < 50)
				moveTo(90);
			else if(getX() > getBattleFieldWidth()-50)
				moveTo(270);
			else if(getY() < 50)
				moveTo(0);
			else if(getY() > getBattleFieldHeight()-50)
				moveTo(180);
			else if( e.getDistance() > 200 )
				moveTo(e.getBearing()+getHeading()+MOUVEMENT_LATERAL*randMove);
			else if( e.getDistance() < 100 )
				moveTo(e.getBearing()+getHeading()+MOUVEMENT_LATERAL*randMove+MOUVEMENT_BACK);
			else
				moveTo(e.getBearing()+getHeading()+90*randMove);
		
			scan();
		}
		else{
		}
	}


	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e)
	{
		if(!melee){
		randDeplacement();		
		randFrequencity+=10;
		}
		else{
		}
	}
	
	public void onHitRobot(HitRobotEvent e)
	{
		if(!melee)
		{
			turningRadar = true;
			setTurnRadarRight(normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading())));
		}
	}
	public void randDeplacement(){
		
		if(!melee)
		{
			rand = (int)(Math.random()*Rules.MAX_VELOCITY);
			if(rand<MINI_VITESSE)rand=Rules.MAX_VELOCITY;
			setMaxVelocity(rand);
		}
	}
	public void onWin(WinEvent event)
	{/*
		RobocodeFileWriter f;
		try
		{
			f = new RobocodeFileWriter("test.txt");
			f.write(totRand/nbRand);
			f.close();
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		
		}*/
		/*out.println("victoire : " +totRand/nbRand);
		PrintStream w = null;

		try {
			w = new PrintStream(new RobocodeFileOutputStream(getDataFile("count.dat")));

			w.println(totRand/nbRand);
			
			// PrintStreams don't throw IOExceptions during prints,
			// they simply set a flag.... so check it here.
			if (w.checkError()) {
				out.println("I could not write the count!");
			}
		} catch (IOException e) {
			out.println("IOException trying to write: ");
			e.printStackTrace(out);
		} finally {
			if (w != null) {
				w.close();
			}
	}			*/		
		if(!melee){			
			stop();
			setTurnRight(1000);
			setTurnGunLeft(1000);
		}
	}
	public void onRoundEnded(RoundEndedEvent event) {
		
		//out.println(totRand/nbRand);
	}



	public void changeColor()
	{
		if(!melee){
			
			curColor = curColor+20*colorSens;
			if(curColor >=200)
			{	
				curColor = 200;
				colorSens*=-1;
			}
			else if(curColor <=0)
			{	
				curColor = 0;
				colorSens*=-1;
			}
		
			Color c= new Color(curColor, curColor, 0);
			setColors(c,c,c,c,c);
		}
	}

	public void moveTo(double angle)
	{
		if(!melee){
			
			if(Math.abs(normalRelativeAngleDegrees(angle - getHeading())) <= 90)
			{
				setTurnRight(normalRelativeAngleDegrees(angle - getHeading()));
				setAhead(1000);
			}
	
			else
			{
				setTurnRight(normalRelativeAngleDegrees(angle - getHeading()+180));
				setBack(1000);
			}
		 }
	}

	public double getBulletPowerFor(ScannedRobotEvent e)
	{
		if(!melee){
			
			if(e.getDistance() < DIST_COURS/*150*/)
				return Rules.MAX_BULLET_POWER;
			else if(e.getDistance() < DIST_MOYEN/*225*/)
				return 2;
			else
				return 1;
		}
		else{
			return 0;
		}
}
}
																																