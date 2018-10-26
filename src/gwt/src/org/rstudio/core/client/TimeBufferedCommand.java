/*
 * TimeBufferedCommand.java
 *
 * Copyright (C) 2009-18 by RStudio, Inc.
 *
 * Unless you have received this program directly from RStudio pursuant
 * to the terms of a commercial license agreement with RStudio, then
 * this program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */
package org.rstudio.core.client;

import org.rstudio.core.client.dom.WindowEx;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.user.client.Timer;

/**
 * Manages the execution of logic that should not be run too frequently.
 * Multiple calls over a (caller-defined) period of time will be coalesced
 * into one call.
 *
 * The command can optionally be run on a scheduled ("passive") basis; use
 * the two- or three-arg constructor. IMPORTANT NOTE: The implementation of
 * performAction must check if shouldReschedule is true, and if it is,
 * then it should call reschedule() whenever it is done with the
 * operation. Failure to do so correctly (e.g. in error cases) will cause
 * the passive runs to immediately stop occurring. 
 */
public abstract class TimeBufferedCommand
{
   /**
    * See class javadoc for details about shouldReschedule flag.
    */
   protected abstract void performAction(boolean shouldReschedule);
   
   /**
    * Creates a TimeBufferedCommand that will only run when nudged.
    */
   public TimeBufferedCommand(int activeIntervalMillis)
   {
      this(-1, -1, activeIntervalMillis);
   }

   /**
    * Creates a TimeBufferedCommand that will run when nudged, or every
    * passiveIntervalMillis milliseconds, whichever comes first.
    */
   public TimeBufferedCommand(int passiveIntervalMillis,
                              int activeIntervalMillis)
   {
      this(passiveIntervalMillis, passiveIntervalMillis, activeIntervalMillis);
   }

   /**
    * Creates a TimeBufferedCommand that will run when nudged, or every
    * passiveIntervalMillis milliseconds, whichever comes first; with a
    * custom period before the first "passive" run.
    */
   public TimeBufferedCommand(int initialIntervalMillis,
                              int passiveIntervalMillis,
                              int activeIntervalMillis)
   {
      this.initialIntervalMillis_ = initialIntervalMillis;
      this.passiveIntervalMillis_ = passiveIntervalMillis;
      this.activeIntervalMillis_ = activeIntervalMillis;
      this.timer_ = new Timer()
      {
         @Override
         public void run()
         {
            boolean shouldReschedule = !isSuspended_ && passiveIntervalMillis_ > 0;
            performAction(shouldReschedule);
         }
      };
      
      WindowEx.addFocusHandler((FocusEvent event) -> onFocusChanged(true));
      WindowEx.addBlurHandler((BlurEvent event) -> onFocusChanged(false));

      if (initialIntervalMillis_ >= 0 && passiveIntervalMillis_ > 0)
         timer_.schedule(initialIntervalMillis_);
   }
   
   /**
    * Re-schedule the timer. If the timer is already running, it will be
    * canceled and re-scheduled. Useful if you're receiving a flood of events
    * and want this timer to fire only after all of these events have been
    * processed.
    */
   public final void reschedule()
   {
      timer_.schedule(passiveIntervalMillis_);
   }

   /**
    * Re-schedule the timer. If the timer is already running, this request will
    * be ignored, allowing the timer to run as originally scheduled.
    */
   public final void nudge()
   {
      if (!timer_.isRunning())
         timer_.schedule(activeIntervalMillis_);
   }

   /**
    * Suspend execution of the timer.
    */
   public final void suspend()
   {
      isSuspended_ = true;
      timer_.cancel();
   }

   /**
    * Resume execution of the timer.
    */
   public final void resume()
   {
      assert passiveIntervalMillis_ > 0 : "Cannot resume an active-only TimeBufferedCommand";
      isSuspended_ = false;
      timer_.schedule(passiveIntervalMillis_);
   }
   
   private final int initialIntervalMillis_;
   private final int passiveIntervalMillis_;
   private final int activeIntervalMillis_;
   private final Timer timer_;
   private boolean isSuspended_;
   
}
