========================================================================
    APPLICATION LIBRARY : VBS3ZeroMqPosPlugin Project Overview
========================================================================

/////////////////////////////////////////////////////////////////////////////

This is a project for reading positional data from VBS3 and then bundling
it in a Google Protocl Buffer wrapper which will then be broadcast using
ZeroMq.

This project builds under VS 2008 with a Win32 and x64 platform target.

BUILD:
Slect "RELEASE" configuration and the desired platform (Win32/x64).
This project is configured to put the output dll directly into the VBS3
plugin directory:
Win32: C:\Bohemia Interactive\VBS3\plugin
x64: C:\Bohemia Interactive\VBS3\plugin64

REQUIREMENTS:
The correct pthread and zeromq platform dlls must be placed in the Windows
System PATH for the dll to be loaded successfully by VBS3.

RUNNING:
Start VBS3
View Extendsions
Ensure that vbs3zeromqposplugin is loaded

/////////////////////////////////////////////////////////////////////////////
