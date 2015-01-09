/*
 *   Stopwatch
 *
 *   Copyright (c) 2014 Thierry Margenstern under MIT license
 *   http://opensource.org/licenses/MIT
 */

package tm.android.chronos.core;

/**
 *
 */
public class Stopwatch extends AbstractClock {

    private StopwatchData stopwatchData;
    private StopwatchRenderDelegate<Digit> stopwatchRenderDelegate;
    private boolean noRunRenderUpdate = true;


    public Stopwatch(StopwatchRenderDelegate<Digit> stopwatchRenderDelegate) {
        super();
        stopwatchData = new StopwatchData(getName());
        this.stopwatchRenderDelegate = stopwatchRenderDelegate;

    }


    @Override
    protected void innerRun() throws InterruptedException {
        sleep(20);
        long diff = System.currentTimeMillis() - lastTime;
        currentTime.addMillisSeconds(diff);
        lastTime += diff;
        stopwatchRenderDelegate.doDrawRun(currentTime);
    }

    @Override
    protected void noRunRender() {
        stopwatchRenderDelegate.doDrawNoRun(currentTime, noRunRenderUpdate);
        noRunRenderUpdate = false;

    }

    @Override
    protected void finalRun() {
        stopwatchRenderDelegate.doFinal();
    }

    @Override
    protected void innerStop() {
        try {
            sleep(20);
        } catch (InterruptedException e) {
            //
        }
        currentTime = Digit.split(stopTime - startTime);
        stopwatchData.setChronoTime(currentTime.getInternal());

        noRunRenderUpdate = true;

    }

    public void intermediateTime(long now) {
        stopwatchData.add(now - startTime);
    }

    public StopwatchData getStopwatchData() {
        return stopwatchData;
    }

    @Override
    protected void innerPause() {

    }

    @Override
    protected void innerRestart() {

    }

    @Override
    protected void innerReset() {
        stopwatchData.reset();
        noRunRenderUpdate = true;

    }

    public void setNoRunRenderUpdate(boolean noRunRenderUpate) {
        this.noRunRenderUpdate = noRunRenderUpate;
    }


    public Stopwatch RebuildAndStart(){
        Stopwatch ret = new Stopwatch(stopwatchRenderDelegate);
        ret.startTime = startTime;
        ret.currentTime = Digit.split(0);
        ret.lastTime = startTime;
        ret.setName(getName());
        ret.setNoRunRenderUpdate(false);
        ret.setRunning();
        ret.startLoop();

        return ret;

    }
}
