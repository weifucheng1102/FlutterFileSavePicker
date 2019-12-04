#import "FileSavePickerPlugin.h"

@implementation FileSavePickerPlugin {
    FlutterResult _result;
    NSDictionary *_arguments;
    UIViewController *_viewController;
}
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar> *)registrar {
    FlutterMethodChannel *channel =
    [FlutterMethodChannel methodChannelWithName:@"save_file"
                                binaryMessenger:[registrar messenger]];
    FileSavePickerPlugin *instance =
    [[FileSavePickerPlugin alloc] init];
    [registrar addMethodCallDelegate:instance channel:channel];
}
- (void)handleMethodCall:(FlutterMethodCall *)call result:(FlutterResult)result {
    if (_result) {
        _result([FlutterError errorWithCode:@"multiple_request"
                                    message:@"Cancelled by a second request"
                                    details:nil]);
        _result = nil;
    }
 if ([@"saveVideo" isEqualToString:call.method]) {
        _result = result;
        _arguments = call.arguments;
        NSLog(@"保存");
        NSString * videoPath = [_arguments objectForKey:@"path"] ;
        
        if (UIVideoAtPathIsCompatibleWithSavedPhotosAlbum(videoPath)) {
            //保存视频到相簿
            UISaveVideoAtPathToSavedPhotosAlbum(videoPath, self,
                   @selector(video:didFinishSavingWithError:contextInfo:), nil);
            result(@"成功");
        }
    }
    else {
        result(FlutterMethodNotImplemented);
    }
}
- (void)video:(NSString *)videoPath
        didFinishSavingWithError:(NSError *)error
  contextInfo:(void *)contextInfo{
    
}


@end

