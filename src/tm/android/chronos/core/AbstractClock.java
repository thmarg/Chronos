/*  AbstractClock
 *
 *  Copyright (c) 2014 Thierry Margenstern under MIT license
 *  http://opensource.org/licenses/MIT
 */
package tm.android.chronos.core;

import static tm.android.chronos.core.AbstractClock.STATUS.*;


/**
 * Class AbstractClock implement basic stuff define by interface @see{Clock}.<br>
 * It also provide runtime loop and abstract method to represent time movement so that it can be seen on a UI.
 */
public abstract class AbstractClock extends Thread implements Clock {

    static enum STATUS {RUNNING, STOPPED, WAIT_TO_START, KILLED,PAUSED}


    private STATUS status;


    protected long startTime;// start time
    protected long stopTime;// final time

    protected boolean run = false;

    private boolean error;
    private String errorText;

    protected Digit currentTime;
    protected long lastTime;

    private DeadLineListener countDownListener;

    public AbstractClock() {
        status = WAIT_TO_START;
        currentTime = new Digit(0);
    }


    protected abstract void innerRun() throws InterruptedException;

    protected abstract void finalRun();

    protected abstract void noRunRender();

    @Override
    public void run() {
        while (run) {

            try {
                if (isRunning()) {
                    innerRun();
                } else {
                    noRunRender();
                    sleep(100);
                }

            } catch (InterruptedException e) {
                error = true;
                errorText = e.getLocalizedMessage();
                run = false;
            } finally {
                finalRun();
            }

        }
    }


    public Digit getTime() {
       if (isStopped())
           return Digit.split(stopTime-startTime);
        return currentTime;
    }

    @Override
    public boolean isRunning() {
        return status == RUNNING;
    }

    @Override
    public boolean isStopped() {
        return status == STOPPED;
    }

    @Override
    public boolean isWaitingStart() {
        return status == WAIT_TO_START;
    }

    protected abstract void innerReset();


    @Override
    public void reset() {
        if (status == STOPPED) {
            status = WAIT_TO_START;
            startTime = 0;
            stopTime = 0;
            currentTime.reset();
            innerReset();
        }
    }


    public void startLoop() {
        run = true;
        start();
    }

    @Override
    public void start(long startTime) {
            if (status == WAIT_TO_START) {
                this.startTime = startTime;
                lastTime = startTime;
                status = RUNNING;
            }

    }

    protected abstract void innerStop();

    @Override
    public void stopTime(long stopTime) {
        if (status == RUNNING) {
            status = STOPPED;
            this.stopTime = stopTime;

            innerStop();


        }

    }

    @Override
    public void fullStop() {
        run = false;
        status = KILLED;
        boolean retry = true;
        while (retry)
            try {
                join();
                retry = false;
            } catch (InterruptedException e) {
                //
            }
    }

    @Override
    public boolean isPaused() {
        return status == PAUSED;
    }

    protected abstract void innerPause();

    @Override
    public void pause() {
        if (isRunning()) {
            status = PAUSED;
            innerPause();
        }
    }

    protected abstract void innerRestart();

    @Override
    public void restart() {
        if (isPaused()) {
            innerRestart();
            status = RUNNING;
        }
    }

    public boolean hasError() {
        return error;
    }

    public String getError() {
        return errorText;
    }

    public void addCountDownListener(DeadLineListener countDownListener) {
        this.countDownListener = countDownListener;
    }

    protected void sendDeadLineReached() {
        if (countDownListener != null)
            countDownListener.deadLineReached();
    }

    protected void setRunning(){
        status = RUNNING;
    }
    
    /**
     * Convenience method only for TEST
     *
     * @return an instance of raw type AbstractClock
     */
    public static AbstractClock getAbstractClock() {
        return new AbstractClock() {
            @Override
            protected void innerPause() {

            }

            @Override
            protected void innerRestart() {

            }

            @Override
            protected void finalRun() {

            }

            @Override
            protected void innerRun() {

            }

            @Override
            protected void innerStop() {

            }

            @Override
            protected void noRunRender() {

            }

            @Override
            public void innerReset() {

            }
        };
    }

}
