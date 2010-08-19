package org.openstreetmap.josm.plugins.videomapping.video;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.plugins.videomapping.GpsPlayer;
import org.openstreetmap.josm.plugins.videomapping.PlayerObserver;

//combines video and GPS playback, major control has the video player
public class GPSVideoPlayer implements PlayerObserver{
    Timer t;
    TimerTask syncGPSTrack;
    private GpsPlayer gps;
    private SimpleVideoPlayer video;
    private JButton syncBtn;
    private GPSVideoFile file;
    private boolean synced=false; //do we playback the players together?
    private JCheckBoxMenuItem subtTitleComponent;
    

    public GPSVideoPlayer(File f, final GpsPlayer pl) {
        super();
        this.gps = pl;
        //test sync
        video = new SimpleVideoPlayer();
        /*
        long gpsT=(9*60+20)*1000;
        long videoT=10*60*1000+5*1000;
        setFile(new GPSVideoFile(f, gpsT-videoT)); */
        setFile(new GPSVideoFile(f, 0L));
        //add Sync Button to the Player
        syncBtn= new JButton("sync");
        syncBtn.setBackground(Color.RED);
        syncBtn.addActionListener(new ActionListener() {
            //do a sync
            public void actionPerformed(ActionEvent e) {
                long diff=gps.getRelativeTime()-video.getTime();
                file= new GPSVideoFile(file, diff);
                syncBtn.setBackground(Color.GREEN);
                synced=true;
                markSyncedPoints();
                video.play();
                //gps.play();
            }
        });
        setAsyncMode(true);
        video.addComponent(syncBtn);
        //a observer to communicate
        SimpleVideoPlayer.addObserver(new PlayerObserver() { //TODO has o become this

            public void playing(long time) {
                //sync the GPS back
                if(synced) gps.jump(getGPSTime(time));			
            }

            public void jumping(long time) {
            
            }

            //a push way to set video attirbutes
            public void metadata(long time, boolean subtitles) {
                if(subtTitleComponent!=null) subtTitleComponent.setSelected(subtitles);				
            }
            
        });
        t = new Timer();		
    }
    
    //marks all points that are covered by video AND GPS track
    private void markSyncedPoints() {
        //GPS or video stream starts first in time?
        WayPoint start,end;
        long t;
        if(gps.getLength()<video.getLength())
        {
            //GPS is within video timeperiod
            start=gps.getWaypoint(0);
            end=gps.getWaypoint(gps.getLength());			
        }
        else
        {
            //video is within gps timeperiod
            t=getGPSTime(0);
            if(t<0) t=0;
            start=gps.getWaypoint(t);
            end=gps.getWaypoint(getGPSTime(video.getLength()));
        }
        //mark as synced
        List<WayPoint> ls = gps.getTrack().subList(gps.getTrack().indexOf(start), gps.getTrack().indexOf(end));
        
        for (WayPoint wp : ls) {
            wp.attr.put("synced", "true");
        }	
    }

    public void setAsyncMode(boolean b)
    {
        if(b)
        {
            syncBtn.setVisible(true);
        }
        else
        {
            syncBtn.setVisible(false);
        }
    }
    
        
    public void setFile(GPSVideoFile f)
    {
        
        file=f;
        video.setFile(f.getAbsoluteFile());
        //video.play();
    }
    
    public void play(long gpsstart)
    {
        //video is already playing
        jumpToGPSTime(gpsstart);
        gps.jump(gpsstart);
        //gps.play();
    }
    
    public void play()
    {
        video.play();
    }
    
    public void pause()
    {
        video.pause();
    }
    
    //jumps in video to the corresponding linked time
    public void jumpToGPSTime(Time GPSTime)
    {
        gps.jump(GPSTime);
    }
    
    //jumps in video to the corresponding linked time
    public void jumpToGPSTime(long gpsT)
    {
        if(!synced)
        {
            //when not synced we can just move the icon to the right position			
            gps.jump(new Date(gpsT));
            Main.map.mapView.repaint();
        }
        video.jump(getVideoTime(gpsT));
    }
    
    //calc synced timecode from video
    private long getVideoTime(long GPStime)
    {
        return GPStime-file.offset;
    }
    
    //calc corresponding GPS time
    private long getGPSTime(long videoTime)
    {
        return videoTime+file.offset;
    }

    

    public void setJumpLength(Integer integer) {
        video.setJumpLength(integer);
        
    }

    public void setLoopLength(Integer integer) {
        video.setLoopLength(integer);
        
    }

    public void loop() {
        video.loop();
        
    }

    public void forward() {
        video.forward();
        
    }

    public void backward() {
        video.backward();
        
    }

    public void removeVideo() {
        video.removeVideo();
        
    }

    public File getVideo() {
        return file;
    }

    public float getCoverage() {
        return gps.getLength()/video.getLength();
    }

    public void setDeinterlacer(String string) {
        video.setDeinterlacer(string);
        
    }

    public void setAutoCenter(boolean selected) {
        gps.setAutoCenter(selected);
        
    }

    
    //not called by GPS
    public boolean playing() {
        return video.playing();
    }

    //when we clicked on the layer, here we update the video position
    public void jumping(long time) {
        if(synced) jumpToGPSTime(gps.getRelativeTime());
        
    }

    public String getNativePlayerInfos() {
        return video.getNativePlayerInfos();
    }

    public void faster() {
        video.faster();
        
    }

    public void slower() {
        video.slower();
        
    }

    public void playing(long time) {
        // TODO Auto-generated method stub
        
    }

    public void toggleSubtitles() {
        video.toggleSubs();
        
    }
    
    public boolean hasSubtitles(){
        return video.hasSubtitles();
    }

    public void setSubtitleAction(JCheckBoxMenuItem a)
    {
        subtTitleComponent=a;
    }

    public void metadata(long time, boolean subtitles) {
        // TODO Auto-generated method stub
        
    }

    
    

    
}
