Freedomotic
=================================

Official website is http://freedomotic.com

Freedomotic is an open source, flexible, secure Internet of Things (IoT) application framework, useful to build and manage modern smart spaces. It is targeted to private individuals (home automation) as well as business users (smart retail environments, ambient aware marketing, monitoring and analytics, ...)

Freedomotic can run also on **Raspberry Pi** and can easily interact with DIY **Arduino** projects.

Starting from 2 Feb 2014, this is the Freedomotic mainstream repository. Previous repository was hosted on googlecode http://code.google.com/p/freedomotic

Requirements: 
- **Java Open JDK** version 8 or oracle jdk _(to install on ubuntu: sudo apt-get install openjdk-8-jdk)_
- **Maven** version 2 or 3 _(to install on ubuntu: sudo apt-get install maven)_
- **Any OS** with java support _(Linux, Windows, Mac, Solaris ...)_

Development status:
- **Current released version**: 5.6.0 'Commander RC3' (released on 1 Jul 2016)
- **Version in development (HEAD of this repository)**: 5.6.0 'Commander'


Quick Start
======================

Follow these instructions if you want to compile Freedomotic from source (eg: to develop your own plugins). If you just want to try it, just download the precompiled binaries you can find at the official download page http://freedomotic.com/content/download

**1) Fork Freedomotic on GitHub**

* Create an account on https://github.com if you don't have one.
* **Fork Freedomotic** [_(What does it mean?)_](https://help.github.com/articles/fork-a-repo) following this link: <https://github.com/freedomotic/freedomotic/fork>. 
* Create the local clone of your online fork with this command:

```
git clone https://github.com/YOUR-GITHUB-USERNAME/freedomotic.git
```

Now the repository is ready to work with.

**2) Enter the new local folder**

    cd freedomotic
    
**3) Compile Freedomotic with maven**

    mvn clean install
    
**4) IMPORTANT!!!! THIS IS REQUIRED: Copy the example-data folder into freedomotic-core/data.** If you miss this step Freedomotic won't start

    cp -r data-example/ framework/freedomotic-core/data
    
**5) Run Freedomotic**

    java -jar framework/freedomotic-core/target/freedomotic-core/freedomotic.jar

As an alternative you can start **freedomotic-core** project from your favourite IDE.
    

Getting help
============

Having trouble with Freedomotic? We’d like to help!

- Check out the [user manual](http://freedomotic-user-manual.readthedocs.io) for reference documentation. 
- Write on the [mailing list](https://groups.google.com/forum/#!forum/freedom-domotics)
- Send an email to info@freedomotic.com
- Open an issue on https://github.com/freedomotic/freedomotic/issues

Contributing
============

Want to help us? It's very simple and funny. [Here](https://github.com/freedomotic/freedomotic/blob/master/CONTRIBUTING.md) how to do.

License
=============

Freedomotic is an Open Source software released under the [GNU GPLv2](http://www.gnu.org/licenses/old-licenses/gpl-2.0.html) license.
