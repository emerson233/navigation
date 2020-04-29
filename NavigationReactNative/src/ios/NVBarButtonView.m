#import "NVBarButtonView.h"

#import <UIKit/UIKit.h>
#import <React/UIView+React.h>

@implementation NVBarButtonView

- (id)init
{
    if (self = [super init]) {
        self.button = [[UIBarButtonItem alloc] init];
        self.button.style = UIBarButtonItemStylePlain;
        self.button.target = self;
        self.button.action = @selector(buttonPressed);
    }
    return self;
}

- (void)insertReactSubview:(UIView *)subview atIndex:(NSInteger)atIndex
{
    [super insertReactSubview:subview atIndex:atIndex];
    self.button.customView = subview;
}

- (void)removeReactSubview:(UIView *)subview
{
    [super removeReactSubview:subview];
    self.button.customView = nil;
}

- (void)setTitle:(NSString *)title
{
    self.button.title = title;
}

- (void)setImage:(UIImage *)image
{
    self.button.image = image;
}

- (void)setTintColor:(UIColor *)tintColor
{
    self.button.tintColor = tintColor;
}

- (void)setEnabled:(BOOL)enabled
{
    self.button.enabled = enabled;
}

- (void)setSystemItem:(UIBarButtonSystemItem)systemItem
{
    self.button = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:systemItem target:self action:@selector(buttonPressed)];
}

-(void)buttonPressed
{
    if (!!self.onPress) {
        self.onPress(nil);
    }
}

- (void)setAccessibilityLabel:(NSString *)accessibilityLabel
{
    self.button.accessibilityIdentifier = accessibilityLabel;
}

@end
