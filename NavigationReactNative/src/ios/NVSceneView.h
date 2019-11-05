#import <UIKit/UIKit.h>
#import <React/RCTComponent.h>

@interface NVSceneView : UIView

@property (nonatomic, copy) NSString *sceneKey;
@property (nonatomic, copy) NSString *title;
@property (nonatomic, copy) NSString *navigationID;
@property (nonatomic, copy) RCTDirectEventBlock onWillAppear;
@property (nonatomic, copy) RCTDirectEventBlock onDidAppear;
@property (nonatomic, copy) RCTDirectEventBlock onWillDisappear;
@property (nonatomic, copy) RCTDirectEventBlock onDidDisappear;
@property (nonatomic, copy) RCTDirectEventBlock onPopped;

-(void)willAppear;
-(void)didAppear;
-(void)willDisappear;
-(void)didDisappear;
-(void)didPop;

@end
