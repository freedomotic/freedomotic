/**
 *
 * Copyright (c) 2009-2016 Freedomotic team
 * http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This Program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedomotic; see the file COPYING.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.freedomotic.things.impl;

import com.freedomotic.model.ds.Config;
import com.freedomotic.model.object.BooleanBehavior;
import com.freedomotic.model.object.ListBehavior;
import com.freedomotic.model.object.RangedIntBehavior;
import com.freedomotic.behaviors.BooleanBehaviorLogic;
import com.freedomotic.behaviors.ListBehaviorLogic;
import com.freedomotic.behaviors.RangedIntBehaviorLogic;
import com.freedomotic.reactions.Command;

/**
 *
 * @author gpt
 */

public class TV extends ElectricDevice {

    public RangedIntBehaviorLogic volume;
    public RangedIntBehaviorLogic channel; //Maybe the channell is not a behavior. Only an action. 
    //(In the Kuro TV can't ask to the current channel
    public ListBehaviorLogic input; // I defined it as a RangedIt because each TV model has its own inputs   
    public BooleanBehaviorLogic muted;
    public ListBehaviorLogic avSelection;
    public ListBehaviorLogic screenMode;

    @Override
    public void init() {


        //linking this property with the behavior defined in the XML
        volume = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior("volume"));
        volume.addListener(new RangedIntBehaviorLogic.Listener() {
            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
                //turnPowerOff(params);
            	onRangeValue(volume.getMin(), params, fireCommand);
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
                //turnPowerOn(params);
            	onRangeValue(volume.getMax(), params, fireCommand);
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                if (fireCommand) {
                    executeSetVolume(rangeValue, params);
                } else {
                    setVolume(rangeValue);
                }
            }
        });
        registerBehavior(volume);

        //linking this property with the behavior defined in the XML
        channel = new RangedIntBehaviorLogic((RangedIntBehavior) getPojo().getBehavior("channel"));

        channel.addListener(new RangedIntBehaviorLogic.Listener() {
            @Override
            public void onLowerBoundValue(Config params, boolean fireCommand) {
            	onRangeValue(channel.getMin(), params, fireCommand);
            }

            @Override
            public void onUpperBoundValue(Config params, boolean fireCommand) {
            	onRangeValue(channel.getMax(), params, fireCommand);
            }

            @Override
            public void onRangeValue(int rangeValue, Config params, boolean fireCommand) {
                if (fireCommand) {
                    executeSetChannel(rangeValue, params);
                } else {
                    setChannel(rangeValue);
                }
            }
        });
        registerBehavior(channel);


        //linking this property with the behavior defined in the XML
        input = new ListBehaviorLogic((ListBehavior) getPojo().getBehavior("input"));
        input.addListener(new ListBehaviorLogic.Listener() {
            @Override
            public void selectedChanged(Config params, boolean fireCommand) {
                if (fireCommand) {
                    executeSetInput(params);
                } else {
                    setInput(params.getProperty("value"));
                }
            }
        });
        registerBehavior(input);

        //linking this powered property with the muted behavior defined in the XML
        muted = new BooleanBehaviorLogic((BooleanBehavior) getPojo().getBehavior("muted"));
        muted.addListener(new BooleanBehaviorLogic.Listener() {
            @Override
            public void onTrue(Config params, boolean fireCommand) {
                if (fireCommand) {
                    executeSetMuteOn(params);
                } else {
                    setMuteOn();
                }
            }

            @Override
            public void onFalse(Config params, boolean fireCommand) {
                if (fireCommand) {
                    executeSetMuteOff(params);
                } else {
                    setMuteOff();
                }
            }
        });
        registerBehavior(muted);

        //linking this powered property with the avSelection behavior defined in the XML
        avSelection = new ListBehaviorLogic((ListBehavior) getPojo().getBehavior("avselection"));
        avSelection.addListener(new ListBehaviorLogic.Listener() {
            @Override
            public void selectedChanged(Config params, boolean fireCommand) {
                if (fireCommand) {
                    executeSetAVSelection(params);
                } else {
                    setAVSelection(params.getProperty("value"));
                }
            }
        });
        registerBehavior(avSelection);

        //linking this powered property with the screenMode behavior defined in the XML
        screenMode = new ListBehaviorLogic((ListBehavior) getPojo().getBehavior("screenMode"));
        screenMode.addListener(new ListBehaviorLogic.Listener() {
            //TODO: in the kuro the screen modes available depends of the source.
            @Override
            public void selectedChanged(Config params, boolean fireCommand) {
                if (fireCommand) {
                    executeSetScreenMode(params);
                } else {
                    setScreenMode(params.getProperty("value"));
                }
            }
        });
        registerBehavior(screenMode);
        super.init();
    }

    public void executeSetVolume(int rangeValue, Config params) {
        boolean executed = executeCommand("set volume", params); //executes the developer level command associated with 'set volume' action
        if (executed) {
            setVolume(rangeValue);
            //TODO: set the light graphical representation
            //setCurrentRepresentation(1); //points to the second element in the XML views array (light on image)
            setChanged(true);
        }
    }

    public void setVolume(int rangeValue) {
        if (volume.getValue() != rangeValue) {
            volume.setValue(rangeValue);
            setChanged(true);
        }
    }

    public void setVolumeUp(Config params) {
        boolean executed = executeCommand("set volume up", params); //executes the developer level command associated with 'set channel' action
        if (executed) {
            if (volume.getValue() != volume.getMax()) {
                volume.setValue(volume.getValue() + 1);
                //TODO: set the TV graphical representation
                //setView(1); //points to the second element in the XML views array (light on image)
                setChanged(true);
            }
        }
    }

    public void setVolumeDown(Config params) {
        boolean executed = executeCommand("set volume down", params); //executes the developer level command associated with 'set channel' action
        if (executed) {
            if (volume.getValue() != volume.getMin()) {
                volume.setValue(volume.getValue() - 1);
                //TODO: set the TV graphical representation
                //setView(1); //points to the second element in the XML views array (light on image)
                setChanged(true);
            }
        }
    }

    public void executeSetChannel(int rangeValue, Config params) {
        boolean executed = executeCommand("set channel", params); //executes the developer level command associated with 'set volume' action
        if (executed) {
            setChannel(rangeValue);
            setChanged(true);
        }

    }

    public void setChannel(int rangeValue) {
        if (channel.getValue() != rangeValue) {
            channel.setValue(rangeValue);
            setChanged(true);
        }
    }

    public void setChannelUp(Config params) {
        boolean executed = executeCommand("set channel up", params); //executes the developer level command associated with 'set channel' action
        if (executed) {
            if (channel.getValue() != channel.getMax()) {
                channel.setValue(channel.getValue() + 1);
                powered.setValue(true); //select a channel turn on the tv automatically. This is for coherence.
                //TODO: set the TV graphical representation
                //setView(1); //points to the second element in the XML views array (light on image)
                setChanged(true);
            }
        }
    }

    public void setChannelDown(Config params) {
        boolean executed = executeCommand("set channel down", params); //executes the developer level command associated with 'set channel' action
        if (executed) {
            if (channel.getValue() != channel.getMin()) {
                channel.setValue(channel.getValue() - 1);
                powered.setValue(true); //select a channel turn on the tv automatically. This is for coherence.
                //TODO: set the TV graphical representation
                //setView(1); //points to the second element in the XML views array (light on image)
                setChanged(true);
            }
        }
    }

    public void executeSetInput(Config params) {
        boolean executed = executeCommand("set input", params);
        if (executed) {
            setInput(params.getProperty("value"));
            setChanged(true);
        }
    }

    public void setInput(String value) {
        if (!input.getSelected().equals(value)) {
            input.setSelected(value);
            setChanged(true);
        }
    }

    public void executeSetMuteOn(Config params) {
        boolean executed = executeCommand("mute on", params);
        if (executed) {
            setMuteOn();
            setChanged(true);
        }

    }

    public void setMuteOn() {
        if (muted.getValue() != true) {
            muted.setValue(true);
            setChanged(true);
        }
    }

    public void executeSetMuteOff(Config params) {
        boolean executed = executeCommand("mute off", params);
        if (executed) {
            setMuteOff();
            setChanged(true);
        }

    }

    public void setMuteOff() {
        if (muted.getValue() != false) {
            muted.setValue(false);
            setChanged(true);
        }
    }

    public void executeSetAVSelection(Config params) {
        boolean executed = executeCommand("set avselection", params);
        if (executed) {
            setAVSelection(params.getProperty("value"));
            setChanged(true);
        }
    }

    public void setAVSelection(String value) {
        if (!avSelection.getSelected().equals(value)) {
            avSelection.setSelected(value);
            setChanged(true);
        }
    }

    public void executeSetScreenMode(Config params) {
        boolean executed = executeCommand("set screenmode", params);
        if (executed) {
            setScreenMode(params.getProperty("value"));
            setChanged(true);
        }
    }

    public void setScreenMode(String value) {
        if (!screenMode.getSelected().equals(value)) {
            screenMode.setSelected(value);
            setChanged(true);
        }
    }

    @Override
    protected void createCommands() {
        super.createCommands();

        Command a = new Command();
        a.setName("Set " + getPojo().getName() + " volume to 50%");
        a.setDescription("the TV " + getPojo().getName() + " changes its volume to 50%");
        a.setReceiver("app.events.sensors.behavior.request.objects");
        a.setProperty("object", getPojo().getName());
        a.setProperty("behavior", "volume");
        a.setProperty("value", "50");

        Command b = new Command();
        b.setName(getPojo().getName() + " volume up");
        b.setDescription("increases " + getPojo().getName() + " volume of one step");
        b.setReceiver("app.events.sensors.behavior.request.objects");
        b.setProperty("object", getPojo().getName());
        b.setProperty("behavior", "volume");
        b.setProperty("value", "next");

        Command c = new Command();
        c.setName(getPojo().getName() + " volume down");
        c.setDescription("decreases " + getPojo().getName() + " volume of one step");
        c.setReceiver("app.events.sensors.behavior.request.objects");
        c.setProperty("object", getPojo().getName());
        c.setProperty("behavior", "volume");
        c.setProperty("value", "previous");

        Command d = new Command();
        d.setName("Set its volume to 50%");
        d.setDescription("set its volume to 50%");
        d.setReceiver("app.events.sensors.behavior.request.objects");
        d.setProperty("object", "@event.object.name");
        d.setProperty("behavior", "volume");
        d.setProperty("value", "50");

        Command e = new Command();
        e.setName("Increase its volume");
        e.setDescription("increases its volume of one step");
        e.setReceiver("app.events.sensors.behavior.request.objects");
        e.setProperty("object", "@event.object.name");
        e.setProperty("behavior", "volume");
        e.setProperty("value", "next");

        Command f = new Command();
        f.setName("Decrease its volume");
        f.setDescription("decreases its volume of one step");
        f.setReceiver("app.events.sensors.behavior.request.objects");
        f.setProperty("object", "@event.object.name");
        f.setProperty("behavior", "volume");
        f.setProperty("value", "previous");


        Command g = new Command();
        g.setName("Set its volume to the value in the event");
        g.setDescription("set its volume to the value in the event");
        g.setReceiver("app.events.sensors.behavior.request.objects");
        g.setProperty("object", "@event.object.name");
        g.setProperty("behavior", "volume");
        g.setProperty("value", "@event.value");

        Command h = new Command();
        h.setName(getPojo().getName() + " channel down");
        h.setDescription("turns " + getPojo().getName() + " to the previous channel in the list");
        h.setReceiver("app.events.sensors.behavior.request.objects");
        h.setProperty("object", getPojo().getName());
        h.setProperty("behavior", "channel");
        h.setProperty("value", "previous");

        Command i = new Command();
        i.setName("Mute " + getPojo().getName());
        i.setDescription("mutes the volume of " + getPojo().getName());
        i.setReceiver("app.events.sensors.behavior.request.objects");
        i.setProperty("object", getPojo().getName());
        i.setProperty("behavior", "muted");
        i.setProperty("value", "true");

        Command l = new Command();
        l.setName("Unmute " + getPojo().getName());
        l.setDescription("unmutes the volume of " + getPojo().getName());
        l.setReceiver("app.events.sensors.behavior.request.objects");
        l.setProperty("object", getPojo().getName());
        l.setProperty("behavior", "muted");
        l.setProperty("value", "false");

        Command m = new Command();
        m.setName("Switch muted state for " + getPojo().getName());
        m.setDescription("switches unmuted state of " + getPojo().getName());
        m.setReceiver("app.events.sensors.behavior.request.objects");
        m.setProperty("object", getPojo().getName());
        m.setProperty("behavior", "muted");
        m.setProperty("value", "opposite");

        Command n = new Command();
        n.setName(getPojo().getName() + " channel up");
        n.setDescription("turns " + getPojo().getName() + " to the next channel in the list");
        n.setReceiver("app.events.sensors.behavior.request.objects");
        n.setProperty("object", getPojo().getName());
        n.setProperty("behavior", "channel");
        n.setProperty("value", "next");

        //avSelection related commands
        Command o = new Command();
        o.setName(getPojo().getName() + " next AV Selection");
        o.setDescription("select the " + getPojo().getName() + " next AV Selection");
        o.setReceiver("app.events.sensors.behavior.request.objects");
        o.setProperty("object", getPojo().getName());
        o.setProperty("behavior", "avSelection");
        o.setProperty("value", "next");

        Command p = new Command();
        p.setName(getPojo().getName() + " previous AV Selection");
        p.setDescription("select the " + getPojo().getName() + " previous AV Selection");
        p.setReceiver("app.events.sensors.behavior.request.objects");
        p.setProperty("object", getPojo().getName());
        p.setProperty("behavior", "avSelection");
        p.setProperty("value", "previous");

        //screenMode related commands
        Command q = new Command();
        q.setName(getPojo().getName() + " next Screen Mode");
        q.setDescription("select the " + getPojo().getName() + " next Screen Mode");
        q.setReceiver("app.events.sensors.behavior.request.objects");
        q.setProperty("object", getPojo().getName());
        q.setProperty("behavior", "screenMode");
        q.setProperty("value", "next");

        Command r = new Command();
        r.setName(getPojo().getName() + " previous Screen Mode");
        r.setDescription("select the " + getPojo().getName() + " previous Screen Mode");
        r.setReceiver("app.events.sensors.behavior.request.objects");
        r.setProperty("object", getPojo().getName());
        r.setProperty("behavior", "screenMode");
        r.setProperty("value", "previous");

        //screenMode related commands
        Command s = new Command();
        s.setName(getPojo().getName() + " next Input");
        s.setDescription("select the " + getPojo().getName() + " next Input");
        s.setReceiver("app.events.sensors.behavior.request.objects");
        s.setProperty("object", getPojo().getName());
        s.setProperty("behavior", "input");
        s.setProperty("value", "next");

        Command t = new Command();
        t.setName(getPojo().getName() + " previous Input");
        t.setDescription("select the " + getPojo().getName() + " previous Input");
        t.setReceiver("app.events.sensors.behavior.request.objects");
        t.setProperty("object", getPojo().getName());
        t.setProperty("behavior", "input");
        t.setProperty("value", "previous");

        commandRepository.create(a);
        commandRepository.create(b);
        commandRepository.create(c);
        commandRepository.create(d);
        commandRepository.create(e);
        commandRepository.create(f);
        commandRepository.create(g);
        commandRepository.create(h);
        commandRepository.create(i);
        commandRepository.create(l);
        commandRepository.create(m);
        commandRepository.create(n);
        commandRepository.create(o);
        commandRepository.create(p);
        commandRepository.create(q);
        commandRepository.create(r);
        commandRepository.create(s);
        commandRepository.create(t);
    }
}
