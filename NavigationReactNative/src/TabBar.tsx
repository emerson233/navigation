import React from 'react';
import { requireNativeComponent, Platform, StyleSheet, View } from 'react-native';
import BackButton from './BackButton';

class TabBar extends React.Component<any, any> {
    constructor(props) {
        super(props);
        this.state = {selectedTab: 0};
    }
    static defaultProps = {
        scrollable: false,
        primary: Platform.OS === 'ios',
    }
    render() {
        var {children, barTintColor, selectedTintColor, unselectedTintColor, bottomTabs, scrollable, primary, swipeable} = this.props;
        bottomTabs = bottomTabs != null ? bottomTabs : primary;
        primary = (Platform.OS === 'android' && swipeable != null) ? !swipeable : primary;
        var tabBarItems = React.Children.toArray(children).filter(child => !!child);
        var titleOnly = !tabBarItems.find(({props}: any) => props.title && props.image);
        var tabViewHeight = !primary ? (titleOnly ? 48 : 72) : 56
        tabViewHeight = Platform.OS === 'android' ? tabViewHeight : 28;
        var TabBar = (Platform.OS === 'android' || primary) ? NVTabBar : View;
        var TabView = !primary ? NVTabLayout : NVTabNavigation;
        TabView = Platform.OS === 'android' ? TabView : NVSegmentedTab;
        var tabLayout = (Platform.OS === 'android' || !primary) && (
            <TabView
                bottomTabs={bottomTabs}
                selectedTintColor={selectedTintColor}
                unselectedTintColor={unselectedTintColor}
                selectedIndicatorAtTop={bottomTabs}
                titles={tabBarItems.map(({props}: any) => props.title)}
                scrollable={scrollable}
                style={{
                    height: tabViewHeight,
                    backgroundColor: barTintColor
                }} />
        );
        return (
            <>
                {!bottomTabs && tabLayout}
                {!!tabBarItems.length && <TabBar
                    tabCount={tabBarItems.length}
                    badges={tabBarItems.map(({props: {badge}}: any) => badge)}
                    onTabSelected={({nativeEvent}) => {
                        if (this.state.selectedTab !== nativeEvent.tab)
                            this.setState({selectedTab: nativeEvent.tab})
                    }}
                    barTintColor={barTintColor}
                    selectedTintColor={selectedTintColor}
                    unselectedTintColor={unselectedTintColor}
                    swipeable={!primary}
                    style={styles.tabBar}>
                        {tabBarItems
                            .filter(child => !!child)
                            .map((child: any, index) => {
                                var selected = index === this.state.selectedTab;
                                return React.cloneElement(child, {...child.props, index, selected})
                            })}
                </TabBar>}
                {bottomTabs && tabLayout}
            </>
        );
    }
}

var NVTabLayout = requireNativeComponent<any>('NVTabLayout', null);
var NVTabNavigation = requireNativeComponent<any>('NVTabNavigation', null);
var NVSegmentedTab = requireNativeComponent<any>('NVSegmentedTab', null);
var NVTabBar = requireNativeComponent<any>('NVTabBar', null);

const styles = StyleSheet.create({
    tabBar: {
        flex: 1,
    },
});

export default TabBar;
