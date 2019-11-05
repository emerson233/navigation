#import "NVSceneManager.h"
#import "NVSceneView.h"

@implementation NVSceneManager

RCT_EXPORT_MODULE()

- (UIView *)view
{
    return [[NVSceneView alloc] init];
}

RCT_EXPORT_VIEW_PROPERTY(sceneKey, NSString)
RCT_EXPORT_VIEW_PROPERTY(title, NSString)
RCT_EXPORT_VIEW_PROPERTY(navigationID, NSString)
RCT_EXPORT_VIEW_PROPERTY(onWillAppear, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onDidAppear, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onWillDisappear, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onDidDisappear, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onPopped, RCTDirectEventBlock)

@end
