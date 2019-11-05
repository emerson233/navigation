#import "NVSceneView.h"

@implementation NVSceneView

- (id)init
{
    if (self = [super init]) {
    }
    return self;
}

-(void)willAppear
{
    self.onWillAppear(nil);
}

-(void)didAppear
{
    self.onDidAppear(nil);
}

-(void)willDisappear
{
    self.onWillDisappear(nil);
}

-(void)didDisappear
{
    self.onDidDisappear(nil);
}

-(void)didPop
{
    self.onPopped(nil);
}

@end
