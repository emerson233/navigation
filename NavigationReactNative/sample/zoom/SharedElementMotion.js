import * as React from 'react';
import { Motion } from 'react-motion';
import { Modal } from 'react-native';
import spring from './spring';

class SharedElementMotion extends React.Component {
    constructor(props, context) {
        super(props, context);
        this.animate = this.animate.bind(this);
        this.reset = this.reset.bind(this);
        this.state = {
            url: this.getStateNavigator().stateContext.url,
            sharedElements: [], animatedElements: {}, force: 1,
        };
    }
    static defaultProps = {
        onAnimating: () => {},
        onAnimated: () => {}
    }
    static contextTypes = {
        stateNavigator: React.PropTypes.object,
        getSharedElements: React.PropTypes.func
    }
    getStateNavigator() {
        return this.props.stateNavigator || this.context.stateNavigator;
    }
    componentDidMount() {
        this.getStateNavigator().onNavigate(this.reset);
        this.animate();
        this.animateTimeout = setTimeout(() => cancelAnimationFrame(this.animateFrame), 300);
    }
    componentWillUnmount() {
        this.getStateNavigator().offNavigate(this.reset);
        cancelAnimationFrame(this.animateFrame);
        clearTimeout(this.animateTimeout);
    }
    animate() {
        this.animateFrame = requestAnimationFrame(() => {
            if (this.state.url !== this.getStateNavigator().stateContext.url)
                return;
            var sharedElements = this.context.getSharedElements();
            if (sharedElements.length !== 0) {
                this.setState({sharedElements}, () => {
                    for(var i = 0; i < sharedElements.length; i++) {
                        var {name, oldElement: old, mountedElement: mounted} = sharedElements[i];
                        if (!this.state.animatedElements[name])
                            this.props.onAnimating(name, old.ref, mounted.ref, old.data, mounted.data);
                    }
                });
            }
            this.animate();
        });
    }
    reset() {
        if (this.state.url === this.getStateNavigator().stateContext.url) {
            this.setState(({force}) => ({sharedElements: [], animatedElements: {}, force: force + 1}));
            cancelAnimationFrame(this.animateFrame);
            clearTimeout(this.animateTimeout);
            this.animate();
            this.animateTimeout = setTimeout(() => cancelAnimationFrame(this.animateFrame), 300);
        }
    }
    stripStyle(style) {
        var newStyle = {};
        for(var key in style) {
            newStyle[key] = style[key].val;
        }
        return newStyle;
    }
    onAnimated(name, old, mounted) {
        this.setState(
            ({animatedElements}) => ({animatedElements: {...animatedElements, [name]: true}}),
            () => {this.props.onAnimated(name, old.ref, mounted.ref, old.data, mounted.data)}
        );
    }
    render() {
        var {children, elementStyle} = this.props;
        var {url, sharedElements, animatedElements, force} = this.state;
        return (url === this.getStateNavigator().stateContext.url &&
            <Modal
                transparent={true} animationType="none" onRequestClose={() => {}}
                visible={sharedElements.length > Object.keys(animatedElements).length}
                supportedOrientations={['portrait', 'landscape', 'landscape-left', 'landscape-right']}>
                {sharedElements.map(({name, oldElement: old, mountedElement: mounted}) => (
                    <Motion
                        key={name} onRest={() => {this.onAnimated(name, old, mounted)}}
                        defaultStyle={{...this.stripStyle(elementStyle(name, {...old.measurements, ...old.data})), __force: 0}}
                        style={{...elementStyle(name, {...mounted.measurements, ...mounted.data}), __force: spring(force)}}>
                        {({__force, ...tweenStyle}) => children(tweenStyle, name, old.data, mounted.data)}
                    </Motion>
                ))}
            </Modal>
        );
    }
}

export default SharedElementMotion;
