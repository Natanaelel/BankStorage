def scale(len, guiscale)

    factorDependingOnTextLength = len >= 4 ? 2.0/3 : len == 3 ? 0.9 : 1.0;
    case (guiscale) 
    when 1
        len >= 4 ? 0.7 : 1.0;
    when 2
        len >= 4 ? 4.0/6 : len >= 3 ? 5.0/6 : 1.0;
    else
        (guiscale * factorDependingOnTextLength).floor.to_f / guiscale;
    end
end

def scale2(len, guiscale)

    factorDependingOnTextLength = len/6 >= 4 ? 0.7 : len/6 == 3 ? 0.9 : 1.0;
    case (guiscale)
    when 1
        len/6 >= 4 ? 0.7 : 1.0;
    when 2
        len/6 >= 4 ? 4.0/6 : len/6 >= 3 ? 5.0/6 : 1.0;
    else
        (guiscale * factorDependingOnTextLength).floor.to_f / guiscale;
    end
end

def scale3(width, guiscale)
    scale = 16.0 / width;
    step = [3, guiscale].max;
    [1, [2.0, (scale * step).floor].max.to_f / step].min;
end
def scale4(width, guiscale)
    scale = 17.0 / width;
    step = [3, guiscale].max;
    [1, [2.0, (scale * step).floor].max.to_f / step].min;
end

def scale5(width, guiscale)
    scale = 16.0 / width;
    step = [3, guiscale].max;
    [1,(scale * step).floor.to_f/step].min
end

def clamp(val,min,max) = [[val,min].max,max].min
# def clamp(val,min,max) = [[val,max].min,min].max

# p *(1..5).map{|len|
p *(1..4).map{|x|x*6.0}.map{|len|
    (1..7).map{|guiscale|
        a = scale3(len, guiscale)
        b = scale5(len, guiscale)
        a == b ? nil : [len,guiscale,a,b]
    }
}.flatten(1).compact
# p scale2(4*6.0,6)*4
rw = 4/6.0*4

# p 5/6.0