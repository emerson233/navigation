#import "NVTabBarItemView.h"
#import "NVNavigationStackView.h"

#import <React/UIView+React.h>

@implementation NVTabBarItemView

- (id)init
{
    if (self = [super init]) {
        self.tab = [[UITabBarItem alloc] init];
    }
    return self;
}

- (void)setTitle:(NSString *)title
{
    self.tab.title = title;
}

- (void)setBadge:(NSString *)badge
{
    BOOL isDot = [badge isEqualToString:@"BADGE_DOT"];

    if (isDot) {
        self.tab.badgeValue = @"●";
        self.tab.badgeColor = [UIColor clearColor];
    } else {
        self.tab.badgeValue = badge;
        self.tab.badgeColor = [UIColor redColor];
    }
}

- (void)setBadgeColor:(UIColor *)badgeColor
{
    if ([self.tab.badgeValue isEqualToString:@"●"]) {
        [self.tab setBadgeTextAttributes:@{NSForegroundColorAttributeName: badgeColor} forState:UIControlStateNormal];
    } else {
        self.tab.badgeColor = badgeColor;
    }
}

- (void)setImage:(UIImage *)image
{
    self.tab.image = image;
}

- (void)setSystemItem:(UITabBarSystemItem)systemItem
{
    self.tab = [[UITabBarItem alloc] initWithTabBarSystemItem:systemItem tag:0];
}

- (void)insertReactSubview:(UIView *)subview atIndex:(NSInteger)atIndex
{
    [super insertReactSubview:subview atIndex:atIndex];
    self.navigationController = [(NVNavigationStackView *) subview navigationController];
    self.navigationController.tabBarItem = self.tab;
}

@end
