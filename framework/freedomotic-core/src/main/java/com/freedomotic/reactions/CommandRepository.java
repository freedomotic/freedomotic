/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.reactions;

import com.freedomotic.persistence.Repository;
import java.io.File;
import java.util.List;

/**
 *
 * @author enrico
 */
public interface CommandRepository extends Repository<Command> {

    public List<Command> findHardwareCommands();

    public List<Command> findUserCommands();

    //TODO: remove it, here just for refactoring

    public void loadCommands(File folder);

    public void saveCommands(File folder);
}
