
/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

/**
 * The purpose of this tutorial is to teach developers about the types of common
 * service patterns, to guide the selection of an appropriate pattern, and to
 * ensure that the corresponding abstract class is used as a starting point. (As
 * much of the boilerplate for service creation is provided by the ImageJ
 * software libraries.)
 */
public class CreatingServices {
	
	/*
	 * AbstractPTService - get and create plugins of a single type
	 * 
	 * AbstractSingletonService - a PTService where each plugin is a singleton
	 * 
	 * AbstractTypedService - it is a PTService where the plugins have a single associated datatype
	 * 
	 * AbstractHandlerService - is a SingletonService and a TypedService where each plugin handles a single corresponding datatype
	 * 
	 * AbstractWrapperService - is a TypedService where each plugin wraps an instance of its associated datatype
	 */

}
