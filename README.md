Freedomotic
=================================

Official website is http://freedomotic.com

Freedomotic is a framework for home and building automation which allows to build smart spaces and ambient aware applications. Learn more at http://freedomotic.com

Freedomotic can run also on **Raspberry Pi** and can easily interact with DIY **Arduino** projects.

Starting from 2 Feb 2014, this is the freedomotic mainstream repository. Previous repository was hosted on googlecode http://code.google.com/p/freedomotic

Requirements: 
- **Java Open JDK** version 6 or 7 or oracle jdk (to install on ubuntu: sudo apt-get install openjdk-7-jdk)
- **Maven** version 2 or 3 (to install on ubuntu: sudo apt-get install maven)
- **Any OS** with java support (Linux, Windows, Mac, Solaris ...)

Development status:
- **Current released version**: 5.5.0 'Bender' (released on 24 Jan 2014)
- **Version in development (HEAD of this repository)**: 5.6.0 'Commander'


Developers Quick Start
======================

**1) Clone the GIT repository**

    git clone https://github.com/freedomotic/freedomotic.git
    
**2) Enter the new local folder**

    cd freedomotic
    
**3) Compile freedomotic with maven**

    mvn clean install
    
**4) IMPORTANT!!!! THIS IS REQUIRED: Copy the example-data folder into freedomotic-core/data.** If you miss this step freedomotic won't start

    cp -r data-example/ framework/freedomotic-core/data
    
**5) Run freedomotic**

    java -jar framework/freedomotic-core/target/freedomotic-core/freedomotic.jar

As an alternative you can start **freedomotic-core** project from your favourite IDE. Here an example with NetBeans IDE http://freedomotic.com/content/faq-troubleshooting#Getting_started_with_netbeans
    
If you experience compile or startup errors please refer to http://freedomotic.com/content/faq-troubleshooting#Developers


Git reporitory is an SDK
========================

The GIT repository is a complete SDK with all you need to code and test your freedomotic plugins. Once compiled for the first time open the freedomotic-core project with your favourite IDE and start it to try freedomotic.

To develop your own plugin you can start from the "hello-world" example project included in GIT_ROOT/plugins/devices/hello-world. Open it in your IDE, make some changes and compile. It will be automatically installed into the freedomotic runtime (freedomotic-core project). Just start freedomotic-core to try your latest changes.

Developers Documentation
========================

Read more on the official website http://freedomotic.com/content/developers-getting-started

Try it on Raspberry Pi
======================
We have a script to do it automatically, follow this short tutorial http://freedomotic.com/content/install-freedomotic-raspberry-pi

Give feedback
=============

Please write on the forum http://freedomotic.com/content/forum or reach us by mail at info@freedomotic.com
