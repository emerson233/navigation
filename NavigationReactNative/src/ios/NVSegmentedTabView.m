#import "NVSegmentedTabView.h"
#import "NVTabBarItemView.h"

@implementation NVSegmentedTabView

- (id)init
{
    if (self = [super init]) {
        [self addTarget:self action:@selector(tabPressed) forControlEvents:UIControlEventValueChanged];
    }
    return self;
}

- (void)setTitles:(NSArray<NSString *> *)titles
{
    NSInteger selectedSegmentIndex = MAX(0, self.selectedSegmentIndex);
    [self removeAllSegments];
    for (NSString *title in titles) {
        [self insertSegmentWithTitle:title atIndex:self.numberOfSegments animated:NO];
    }
    self.selectedSegmentIndex = selectedSegmentIndex;
}

- (void)setBackgroundColor:(UIColor *)backgroundColor
{
    [super setTintColor:backgroundColor];
    if (@available(iOS 13.0, *)) {
        [super setBackgroundColor:backgroundColor];
    }
}

- (void)setSelectedTintColor:(UIColor *)selectedTintColor
{
    if (@available(iOS 13.0, *)) {
        NSMutableDictionary *titleAttributes = [[self titleTextAttributesForState:UIControlStateSelected] mutableCopy];
        if (titleAttributes == nil) {
            titleAttributes = @{}.mutableCopy;
        }
        [titleAttributes removeObjectForKey:NSForegroundColorAttributeName];
        if (selectedTintColor != nil) {
            titleAttributes[NSForegroundColorAttributeName] = selectedTintColor;
        }
        [self setTitleTextAttributes:titleAttributes forState:UIControlStateSelected];
    }
}

- (void)setUnselectedTintColor:(UIColor *)unselectedTintColor
{
    if (@available(iOS 13.0, *)) {
        NSMutableDictionary *titleAttributes = [[self titleTextAttributesForState:UIControlStateNormal] mutableCopy];
        if (titleAttributes == nil) {
            titleAttributes = @{}.mutableCopy;
        }
        [titleAttributes removeObjectForKey:NSForegroundColorAttributeName];
        if (unselectedTintColor != nil) {
            titleAttributes[NSForegroundColorAttributeName] = unselectedTintColor;
        }
        [self setTitleTextAttributes:titleAttributes forState:UIControlStateNormal];
    }
}

- (void)didMoveToWindow
{
    [super didMoveToWindow];
    if (!!self.window)
        [self selectTab:NO];
}

- (void)tabPressed
{
    [self selectTab:YES];
}

- (void)selectTab:(BOOL) press
{
    NSInteger tabBarIndex = [self.superview.subviews indexOfObject:self] + (self.bottomTabs ? -1 : 1);
    UIView* tabBar = [self.superview.subviews objectAtIndex:tabBarIndex];
    for(NSInteger i = 0; i < [tabBar.subviews count]; i++) {
        NVTabBarItemView *tabBarItem = (NVTabBarItemView *) [tabBar.subviews objectAtIndex:i];
        tabBarItem.alpha = (i == self.selectedSegmentIndex ? 1 : 0);
        if (press && i == self.selectedSegmentIndex && !!tabBarItem.onPress) {
            tabBarItem.onPress(nil);
        }
    }
}

@end
