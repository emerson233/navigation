import React from 'react';
import { interpolate } from 'd3-interpolate';
import * as Easing from 'd3-ease';

class Motion extends React.Component {
    constructor(props, context) {
        super(props, context);
        this.move = this.move.bind(this);
        this.state = {items: []};
    }
    componentDidMount() {
        this.moveId = requestAnimationFrame(this.move)
    }
    componentWillUnmount() {
        cancelAnimationFrame(this.moveId);            
    }
    move() {
        this.setState(({items: prevItems, update}) => {
            var {data, enter, leave} = this.props;
            var dataByKey = data.reduce((acc, item) => ({...acc, [item.key]: item}), {});
            var itemsByKey = items.reduce((acc, item) => ({...acc, [item.key]: item}), {});
            var tick = performance.now();
            var items = prevItems
                .map(item => {
                    var end = !dataByKey[item.key] ? leave(item.data) : update(item.data);                
                    const equal = areEqual(item.end, end);
                    var progress = equal ? Math.max(Math.min((tick - item.tick) / 500, 1), 0) : 0; 
                    var interpolators = equal ? item.interpolators : this.getInterpolators(item.style, end);
                    var style = this.interpolateStyle(interpolators, end, progress);
                    return {...item, style, end, interpolators, progress, tick};
                })
                .concat(data
                    .filter(item => !itemsByKey[item.key])
                    .map(item => {
                        var style = enter(item);
                        var end = update(item);
                        var interpolators = this.getInterpolators(style, end);
                        return {...item, style, end, interpolators, progress: 0, tick};
                    })
                );
            this.moveId = requestAnimationFrame(this.move);
            return {items};
        })
    }
    areEqual(from, to) {
        if (Object.keys(from).length !== Object.keys(to).length)
            return false;
        for(var key in from) {
            if (from[key] !== to[key])
                return false;
        }
        return true;
    }
    getInterpolators(start, end) {
        var interpolators = {};
        for(var key in start) {
            interpolators[key] = interpolate(start[key], end[key]);
        }
        return interpolators;        
    }
    interpolateStyle(interpolators, end, progress) {
        var style = {};
        for(var key in end) {
            style[key] = interpolators[key](Easing.easeLinear(progress))
        }
        return style;
    }
}

export default Motion;
