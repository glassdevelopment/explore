package uy.edu.fing.proygrad.explore;

import java.util.Date;

/**
 * Created by gonzalomelov on 1/13/15.
 */
public class WorkerThread implements Runnable {

    private String command;

    public WorkerThread(String s){
        this.command=s;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " Start. Time = " + new Date());
        CameraManager.openCamera();
        CameraManager.takePicture();
        System.out.println(Thread.currentThread().getName() + " End. Time = " + new Date());
    }

    @Override
    public String toString(){
        return this.command;
    }
}
