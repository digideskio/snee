


interface __INTERFACE_NAME__
{
	command error_t requestData(nx_int32_t evalEpoch);

	command error_t open();
		
	event void requestDataDone(__TUPLE_TYPE_PTR__ inQueue, int head, int tail, int inQueueSize);
}


