﻿import AsyncStateNavigator from './AsyncStateNavigator';
import { LinkProps } from './Props';

class LinkUtility {
    static getData(stateNavigator: AsyncStateNavigator, navigationData, includeCurrentData: boolean, currentDataKeys: string): any {
        if (currentDataKeys || includeCurrentData) {
            var keys = includeCurrentData ? undefined : currentDataKeys.trim().split(/\s*,\s*/);
            navigationData = stateNavigator.stateContext.includeCurrentData(navigationData, keys);
        }
        return navigationData;
    }

    static isActive(stateNavigator: AsyncStateNavigator, navigationData: any): boolean {
        var active = true;
        for (var key in navigationData) {
            var val = navigationData[key];
            active = active && (val == null || this.areEqual(val, stateNavigator.stateContext.data[key]));
        }
        return active;
    }

    static setActive(active: boolean, props: any, toProps: any) {
        if (!props.activeCssClass && !props.disableActive)
            return;
        if (active && props.activeCssClass)
            toProps.className = (!toProps.className ? '' : toProps.className + ' ') + props.activeCssClass;
        if (active && props.disableActive) {
            toProps.href = null;
            toProps.onClick = null;
        }
    }

    private static areEqual(val: any, currentVal: any): boolean {
        if (currentVal == null)
            return val == null || val === '';
        var valType = Object.prototype.toString.call(val);
        if (valType !== Object.prototype.toString.call(currentVal))
            return false;
        if (valType === '[object Array]') {
            var active = val.length === currentVal.length;
            for(var i = 0; active && i < val.length; i++) {
                active = this.areEqual(val[i], currentVal[i]);
            }
            return active;
        } else {
            return isNaN(val) ? val === currentVal : +val === +currentVal;
        }
    }

    static toHtmlProps(props: any): any {
        var htmlProps = {};
        for(var key in props) {
            if (key !== 'stateNavigator' && key !== 'stateKey' && key !== 'navigationData'
                && key !== 'includeCurrentData' && key !== 'currentDataKeys' && key !== 'activeCssClass'
                && key !== 'disableActive' && key !== 'distance' && key !== 'historyAction'
                && key !== 'navigating' && key !== 'children' && key !== 'defer')
                htmlProps[key] = props[key];
        }
        return htmlProps;
    }
    
    static getOnClick(stateNavigator: AsyncStateNavigator, props: LinkProps, link: string) {
        return e => {
            if (!e.ctrlKey && !e.shiftKey && !e.metaKey && !e.altKey && !e.button) {
                var { navigating, historyAction, defer } = props;
                if (!navigating || navigating(e, link)) {
                    e.preventDefault();
                    stateNavigator.navigateLink(link, historyAction, false, undefined, defer);
                }
            }
        };
    }
}
export default LinkUtility;