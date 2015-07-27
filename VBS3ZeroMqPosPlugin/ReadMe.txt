========================================================================
    APPLICATION LIBRARY : VBS3ZeroMqPosPlugin Project Overview
========================================================================

/////////////////////////////////////////////////////////////////////////////

This is a project for reading positional data from VBS3 and then bundling
it in a Google Protocl Buffer wrapper which will then be broadcast using
ZeroMq.

If linking against czmq:
Must also link agains:
ws2_32.lib
rpcrt4.lib

For pthreads:
pthreadVC2.lib

I was trying to use pthreads so all zmq init and destroy were done in the
same thread, but I could not get pthreads to do this.
If using czmq, it may work (untested sending protobuf) but the
zsock_destroy() hangs.

/////////////////////////////////////////////////////////////////////////////
