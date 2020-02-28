#import "UmengplusPlugin.h"
#if __has_include(<umengplus/umengplus-Swift.h>)
#import <umengplus/umengplus-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "umengplus-Swift.h"
#endif

@implementation UmengplusPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftUmengplusPlugin registerWithRegistrar:registrar];
}
@end
